import numpy as np
import pandas as pd
from sklearn.preprocessing import MinMaxScaler
from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import LSTM, Dense, Dropout

# List of file paths for the input stock data
input_files = [
    "C:/Users/aarno/IdeaProjects/testStock/src/main/resources/nifty50_stock_data.csv",
    "C:/Users/aarno/IdeaProjects/testStock/src/main/resources/nifty100_stock_data.csv",
    "C:/Users/aarno/IdeaProjects/testStock/src/main/resources/nifty200_stock_data.csv"
]

# Corresponding output prediction file paths
output_files = [
    "C:/Users/aarno/IdeaProjects/testStock/src/main/resources/stock_predictions_nifty50.csv",
    "C:/Users/aarno/IdeaProjects/testStock/src/main/resources/stock_predictions_nifty100.csv",
    "C:/Users/aarno/IdeaProjects/testStock/src/main/resources/stock_predictions_nifty200.csv"
]

# Function to run the prediction algorithm
def run_prediction(input_file, output_file):
    # Load the stock data
    data = pd.read_csv(input_file)

    # Strip leading/trailing spaces from column names
    data.columns = data.columns.str.strip()

    # Ensure 'Date' is in datetime format and all price columns are numeric
    data['Date'] = pd.to_datetime(data['Date'].str.strip(), format='%d-%b-%Y')
    data['Close'] = pd.to_numeric(data['Close'], errors='coerce')
    data['Open'] = pd.to_numeric(data['Open'], errors='coerce')
    data['High'] = pd.to_numeric(data['High'], errors='coerce')
    data['Low'] = pd.to_numeric(data['Low'], errors='coerce')

    # Drop rows with missing values
    data = data.dropna().reset_index(drop=True)

    # Sort the data in chronological order
    data = data.sort_values('Date').reset_index(drop=True)

    # Create additional features for volatility and daily returns
    data['Price_Change'] = data['Close'].pct_change().fillna(0)
    data['Volatility'] = data['High'] - data['Low']
    data['Daily_Return'] = data['Close'].diff().fillna(0)

    # Select features including target 'Close' price
    features = data[['Close', 'Open', 'High', 'Low', 'Shares Traded', 'Price_Change', 'Volatility', 'Daily_Return']].values

    # Scale the data
    scaler = MinMaxScaler(feature_range=(0, 1))
    features_scaled = scaler.fit_transform(features)

    # Prepare data using a 15-day rolling window
    sequence_length = 15
    X = []
    y = []

    for i in range(sequence_length, len(features_scaled)):
        X.append(features_scaled[i - sequence_length:i])
        # Predicting the percentage change for a more dynamic target
        y.append(features_scaled[i, 0])  # Predict 'Close'

    X, y = np.array(X), np.array(y)

    # Reshape X for LSTM
    X = np.reshape(X, (X.shape[0], X.shape[1], X.shape[2]))

    # Define the LSTM model
    model = Sequential()
    model.add(LSTM(units=50, return_sequences=True, input_shape=(X.shape[1], X.shape[2])))
    model.add(Dropout(0.2))
    model.add(LSTM(units=50, return_sequences=True))
    model.add(Dropout(0.2))
    model.add(LSTM(units=50))
    model.add(Dropout(0.2))
    model.add(Dense(units=1))

    model.compile(optimizer='adam', loss='mean_squared_error')

    # Train the model
    model.fit(X, y, epochs=50, batch_size=32)

    # Predict the next 15 days
    last_15_days = features_scaled[-sequence_length:]
    prediction_input = np.reshape(last_15_days, (1, sequence_length, X.shape[2]))

    predicted_prices_scaled = []
    for _ in range(15):
        next_price = model.predict(prediction_input)
        # Adding slight randomness for volatility in predicted prices
        next_price_with_variability = next_price[0, 0] + np.random.normal(0, 0.02)  # Adjust noise level as needed
        predicted_prices_scaled.append(next_price_with_variability)

        # Prepare input for the next prediction
        # Reshape next_price to match the number of features (8) before appending
        next_price_reshaped = np.reshape(next_price, (1, 1, 1))  # shape: [1, 1, 1]

        # Repeat the predicted price to match the number of features
        next_price_reshaped = np.repeat(next_price_reshaped, 8, axis=2)  # shape: [1, 1, 8]

        # Now concatenate the reshaped predicted price with the input
        prediction_input = np.append(prediction_input[:, 1:, :], next_price_reshaped, axis=1)

    # Reshape predicted prices to have the same number of features (8)
    predicted_prices_scaled = np.array(predicted_prices_scaled).reshape(-1, 1)

    # Create a matrix with 8 columns, filling the non-close columns with zeros
    predicted_prices_scaled_8_features = np.zeros((predicted_prices_scaled.shape[0], 8))
    predicted_prices_scaled_8_features[:, 0] = predicted_prices_scaled[:, 0]  # Only populate the first column with predictions

    # Inverse transform to get the actual predicted 'Close' prices
    predicted_prices = scaler.inverse_transform(predicted_prices_scaled_8_features)[:, 0]

    # Generate future dates
    last_date = data['Date'].iloc[-1]
    future_dates = [last_date + pd.Timedelta(days=i) for i in range(1, 16)]

    # Save the predictions
    predictions_df = pd.DataFrame({
        'Date': future_dates,
        'Predicted_Close_Price': predicted_prices.flatten()
    })

    predictions_df.to_csv(output_file, index=False)
    print(f"Predictions saved to {output_file}")

# Run the prediction for all the input files and save their results
for input_file, output_file in zip(input_files, output_files):
    run_prediction(input_file, output_file)
