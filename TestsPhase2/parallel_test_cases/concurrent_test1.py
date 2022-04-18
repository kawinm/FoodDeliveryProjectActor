from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
# Check if a customer's wallet balance is consistent
# after we add and deduct the same amount from it concurrently.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def t1(result):  # First concurrent request

    # Add amount 500 to wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8082/addBalance", json={"custId": 301, "amount": 500})

    result["1"] = http_response


def t2(result):  # Second concurrent request

    # Deduct amount 500 from wallet of Customer 301
    http_response = requests.post(
        "http://localhost:8082/deductBalance", json={"custId": 301, "amount": 500})

    result["2"] = http_response


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Check balance of customer 301
    http_response = requests.get(
        f"http://localhost:8082/balance/301")

    if (http_response.status_code != HTTPStatus.OK):
        return 'Fail1'

    res_body = http_response.json()

    cust_id1 = res_body.get("custId")
    balance1 = res_body.get("balance")


    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###

    if result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED:
        return "Fail2"

    # Check balance of customer 301
    http_response = requests.get(
        f"http://localhost:8082/balance/301")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail3'

    res_body = http_response.json()

    cust_id2 = res_body.get("custId")
    
    # Check the final balance
    balance2 = res_body.get("balance")

    # Checks if the initial balance is not same as the final balance
    if balance1 != balance2:
        return "Fail4"

    return 'Pass'


if __name__ == "__main__":

    print(test())