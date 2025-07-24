
from flask import Flask
from flask_mysqldb import MySQL
from flask_cors import CORS
from werkzeug.security import generate_password_hash, check_password_hash
import MySQLdb
from flask import request, jsonify

app = Flask(__name__)
CORS(app)

# MySQL config
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'campususer'
app.config['MYSQL_PASSWORD'] = 'password123'
app.config['MYSQL_DB'] = 'campusconnect'

mysql = MySQL(app)

@app.route('/')
def home():
    return {"status": "Flask is connected to MySQL!"}
from flask import request, jsonify
from werkzeug.security import generate_password_hash, check_password_hash

@app.route('/register', methods=['POST'])
def register():
    name = request.json.get('name')
    email = request.json.get('email')
    password = request.json.get('password')

    if not name or not email or not password:
        return jsonify({"error": "Missing required fields"}), 400

    cur = mysql.connection.cursor()
    try:
        hashed_password = generate_password_hash(password)
        cur.execute("INSERT INTO users (name, email, password) VALUES (%s, %s, %s)", (name, email, hashed_password))
        mysql.connection.commit()
        return jsonify({"message": "User registered successfully"}), 201
    except Exception as e:
        return jsonify({"error": str(e)}), 400
    finally:
        cur.close()

@app.route('/login', methods=['POST'])
def login():
    email = request.json.get('email')
    password = request.json.get('password')

    if not email or not password:
        return jsonify({"error": "Email and password are required"}), 400

    cur = mysql.connection.cursor()
    cur.execute("SELECT password FROM users WHERE email = %s", (email,))
    user = cur.fetchone()
    cur.close()

    if user and check_password_hash(user[0], password):
        return jsonify({"message": "Login successful", "user": email}), 200
    else:
        return jsonify({"error": "Invalid credentials"}), 401

@app.route('/offer_ride', methods=['POST'])
def offer_ride():
    data = request.get_json()

    driver_email = data.get('driver_email')
    origin = data.get('origin')
    destination = data.get('destination')
    date = data.get('date')  # Format: YYYY-MM-DD
    time = data.get('time')  # Format: HH:MM:SS
    seats = data.get('seats_available')

    if not all([driver_email, origin, destination, date, time, seats]):
        return jsonify({"error": "All fields are required"}), 400

    try:
        cur = mysql.connection.cursor()
        cur.execute("""
            INSERT INTO rides (driver_email, origin, destination, date, time, seats_available)
            VALUES (%s, %s, %s, %s, %s, %s)
        """, (driver_email, origin, destination, date, time, seats))
        mysql.connection.commit()
        cur.close()
        return jsonify({"message": "Ride offered successfully"}), 201

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/find_rides', methods=['POST'])
def find_rides():
    data = request.get_json()
    origin = data.get('origin')
    destination = data.get('destination')
    date = data.get('date')

    query = "SELECT id, driver_email, origin, destination, date, time, seats_available FROM rides WHERE date >= CURDATE()"
    params = []

    if origin:
        query += " AND origin = %s"
        params.append(origin)
    if destination:
        query += " AND destination = %s"
        params.append(destination)
    if date:
        query += " AND date = %s"
        params.append(date)

    try:
        cur = mysql.connection.cursor()
        cur.execute(query, tuple(params))
        rows = cur.fetchall()
        cur.close()

        rides = [
            {
                "id": row[0],
                "driver_email": row[1],
                "origin": row[2],
                "destination": row[3],
                "date": row[4].strftime("%Y-%m-%d"),
                "time": str(row[5]),
                "seats_available": row[6]
            }
            for row in rows
        ]
        return jsonify({"rides": rides}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/join_ride', methods=['POST'])
def join_ride():
    data = request.get_json()
    ride_id = data.get('ride_id')
    rider_email = data.get('rider_email')

    if not ride_id or not rider_email:
        return jsonify({"error": "ride_id and rider_email are required"}), 400

    try:
        cur = mysql.connection.cursor()

        # Check if already joined
        cur.execute("SELECT * FROM ride_requests WHERE ride_id = %s AND rider_email = %s", (ride_id, rider_email))
        if cur.fetchone():
            return jsonify({"error": "You have already joined this ride"}), 400

        # Check available seats
        cur.execute("SELECT seats_available FROM rides WHERE id = %s", (ride_id,))
        result = cur.fetchone()
        if not result:
            return jsonify({"error": "Ride not found"}), 404

        seats_available = result[0]
        if seats_available <= 0:
            return jsonify({"error": "No seats available"}), 400

        # Insert ride request
        cur.execute("INSERT INTO ride_requests (ride_id, rider_email) VALUES (%s, %s)", (ride_id, rider_email))

        # Decrease seat count
        cur.execute("UPDATE rides SET seats_available = seats_available - 1 WHERE id = %s", (ride_id,))

        mysql.connection.commit()
        cur.close()

        return jsonify({"message": "Successfully joined the ride"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/my_rides', methods=['POST'])
def my_rides():
    rider_email = request.args.get('email')
    
    if not rider_email:
        return jsonify({"error": "Email is required"}), 400

    try:
        cur = mysql.connection.cursor(MySQLdb.cursors.DictCursor)
        query = """
            SELECT 
                r.id, r.driver_email, r.origin, r.destination, r.date, r.time, r.seats_available
            FROM 
                ride_requests rr
            JOIN 
                rides r ON rr.ride_id = r.id
            WHERE 
                rr.rider_email = %s
            ORDER BY 
                r.date, r.time
        """
        cur.execute(query, (rider_email,))
        rides = cur.fetchall()
        cur.close()

        # Format output
        for ride in rides:
            ride['date'] = ride['date'].strftime("%Y-%m-%d")
            ride['time'] = str(ride['time'])

        return jsonify({"joined_rides": rides}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/my_joined_rides', methods=['GET'])
def my_joined_rides():
    email = request.args.get('email')

    if not email:
        return jsonify({"error": "Missing email parameter"}), 400

    try:
        cur = mysql.connection.cursor(MySQLdb.cursors.DictCursor)
        cur.execute("""
            SELECT r.id, r.driver_email, r.origin, r.destination, r.date, r.time, r.seats_available
            FROM rides r
            JOIN ride_requests rr ON r.id = rr.ride_id
            WHERE rr.rider_email = %s
            ORDER BY r.date, r.time
        """, (email,))
        rows = cur.fetchall()
        cur.close()

        joined_rides = []
        for row in rows:
            joined_rides.append({
                "ride_id": row['id'],
                "driver_email": row['driver_email'],
                "origin": row['origin'],
                "destination": row['destination'],
                "date": row['date'].strftime("%Y-%m-%d"),
                "time": str(row['time']),
                "seats_available": row.get('seats_available', 0)
            })

        return jsonify({"joined_rides": joined_rides}), 200

    except Exception as e:
        return jsonify({"error": f"Failed to fetch joined rides: {str(e)}"}), 500


@app.route('/cancel_ride', methods=['POST'])
def cancel_ride():
    data = request.get_json()
    ride_id = data.get('ride_id')
    rider_email = data.get('rider_email')

    if not ride_id or not rider_email:
        return jsonify({"error": "ride_id and rider_email are required"}), 400

    try:
        cur = mysql.connection.cursor()

        # Delete the ride request
        cur.execute("DELETE FROM ride_requests WHERE ride_id = %s AND rider_email = %s", (ride_id, rider_email))
        
        # Increase seat count
        cur.execute("UPDATE rides SET seats_available = seats_available + 1 WHERE id = %s", (ride_id,))

        mysql.connection.commit()
        cur.close()

        return jsonify({"message": "Ride canceled successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500
@app.route('/create_topic', methods=['POST'])
def create_topic():
    try:
        data = request.get_json(force=True)
        print("DEBUG: Received JSON data:", data)

        author_email = data.get('author_email')
        title = data.get('title')
        # changed to 'content' to match what your Android sends
        message = data.get('content')

        print("DEBUG: Parsed fields -> author_email:", author_email,
              "title:", title, "message:", message)

        if not all([author_email, title, message]):
            print("DEBUG: Missing fields, returning 400")
            return jsonify({"error": "All fields are required"}), 400

        cur = mysql.connection.cursor()
        cur.execute("""
            INSERT INTO discussion_topics (author_email, title, message)
            VALUES (%s, %s, %s)
        """, (author_email, title, message))
        mysql.connection.commit()
        cur.close()

        print("DEBUG: Insert successful")
        return jsonify({"message": "Discussion topic created successfully"}), 201

    except Exception as e:
        print("DEBUG: Exception occurred:", str(e))
        return jsonify({"error": str(e)}), 500


@app.route('/get_topics', methods=['GET'])
def get_topics():
    try:
        cur = mysql.connection.cursor(MySQLdb.cursors.DictCursor)
        cur.execute("SELECT * FROM discussion_topics ORDER BY created_at DESC")
        topics = cur.fetchall()
        cur.close()
        return jsonify({"topics": topics}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/reply_topic', methods=['POST'])
def reply_topic():
    data = request.get_json()
    topic_id = data.get('topic_id')
    replier_email = data.get('replier_email')
    reply = data.get('reply')

    if not all([topic_id, replier_email, reply]):
        return jsonify({"error": "All fields are required"}), 400

    if len(reply) > 1000:
        return jsonify({"error": "Reply too long. Max 1000 characters allowed."}), 400

    try:
        cur = mysql.connection.cursor()
        # Make sure your `discussion_replies` table has a `created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP` column
        cur.execute("""
            INSERT INTO discussion_replies (topic_id, replier_email, reply)
            VALUES (%s, %s, %s)
        """, (topic_id, replier_email, reply))
        mysql.connection.commit()
        cur.close()
        return jsonify({"message": "Reply posted successfully"}), 201

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/get_replies/<int:topic_id>', methods=['GET'])
def get_replies(topic_id):
    try:
        cur = mysql.connection.cursor(MySQLdb.cursors.DictCursor)
        cur.execute("SELECT * FROM discussion_replies WHERE topic_id = %s ORDER BY created_at", (topic_id,))
        replies = cur.fetchall()
        cur.close()
        print("Replies for topic", topic_id, "→", replies)
        return jsonify(replies), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/get_profile/<email>', methods=['GET'])
def get_profile(email):
    try:
        cur = mysql.connection.cursor()
        cur.execute("SELECT name, email, phone, bio FROM users WHERE email = %s", (email,))
        user = cur.fetchone()
        cur.close()

        if user:
            profile = {
                "name": user[0],
                "email": user[1],
                "phone": user[2] or "",
                "bio": user[3] or ""
            }
            return jsonify(profile), 200
        else:
            return jsonify({"error": "User not found"}), 404

    except Exception as e:
        return jsonify({"error": str(e)}), 500


@app.route('/update_profile', methods=['POST'])
def update_profile():
    try:
        data = request.get_json()
        name = data.get('name')
        email = data.get('email')
        phone = data.get('phone', '')
        bio = data.get('bio', '')

        if not email or not name:
            return jsonify({"error": "Email and name are required"}), 400

        cur = mysql.connection.cursor()
        cur.execute("""
            UPDATE users 
            SET name = %s, phone = %s, bio = %s 
            WHERE email = %s
        """, (name, phone, bio, email))
        mysql.connection.commit()
        cur.close()

        return jsonify({"message": "Profile updated successfully"}), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


print(app.url_map)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
