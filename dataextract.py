import pymysql
import json

# Read the JSON data
def read_json(file_path):
    with open(file_path, 'r') as file:
        data = json.load(file)
    return data

# Function to connect to the database
def connect_to_db():
    connection = pymysql.connect(
        host='127.0.0.1',  # Use the IP address instead of 'localhost'
        user='root',        # Your MySQL username
        password='',  # Your MySQL password
        database='biodiversity', # Your database name
        port=3306           # Default MySQL port
    )
    return connection

# Function to insert data into the database
def insert_data(connection, json_data):
    try:
        with connection.cursor() as cursor:
            for item in json_data:
                # Check if 'indicator' and 'country' fields exist and are dictionaries
                if isinstance(item, dict) and 'indicator' in item and isinstance(item['indicator'], dict) and 'value' in item['indicator'] \
                        and 'country' in item and isinstance(item['country'], dict) and 'value' in item['country']:
                    
                    indicator_value = item['indicator']['value']
                    country_value = item['country']['value']
                    date_value = item.get('date', None)  # Default to None if 'date' is missing
                    value_value = item.get('value', None)  # Default to None if 'value' is missing
                    
                    # SQL query to insert data into the table (adjust the table and columns as needed)
                    sql = """
                        INSERT INTO marine_data (indicator, country, date, value)
                        VALUES (%s, %s, %s, %s)
                    """
                    
                    # Execute the query
                    cursor.execute(sql, (indicator_value, country_value, date_value, value_value))
                else:
                    print(f"Missing or malformed 'indicator' or 'country' in record: {item}")
            
            # Commit the transaction
            connection.commit()
            print(f"Inserted {len(json_data)} records successfully.")
    
    except Exception as e:
        print(f"Error: {e}")
        connection.rollback()

# Main function to load JSON and insert into DB
def main():
    # Path to your JSON file
    file_path = 'data.json'  # Replace with the correct path to your JSON file
    
    # Read JSON data
    json_data = read_json(file_path)
    
    # Connect to the database
    connection = connect_to_db()
    
    # Insert data into the database
    insert_data(connection, json_data)
    
    # Close the connection
    connection.close()

if __name__ == "__main__":
    main()
