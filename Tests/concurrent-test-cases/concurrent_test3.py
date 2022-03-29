from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
# Check if the starting order id is 1000 after reinitialize and with an additional order request running concurrently.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

def t1(result):  # First concurrent request

    # Requesting for order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})

    result["1"] = http_response


def t2(result):  # Second concurrent request

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    result["2"] = http_response


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Requesting for order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 302, "restId": 101, "itemId":1, "qty": 1})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail1'

    # Requesting for another order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail2'

    res_body = http_response.json()


    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###

    if result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED:
        return "Fail3"

    # Get status of the second order
    http_response = requests.get(f"http://localhost:8081/order/1001")

    # The second order with order id 1001 should have to be cleared after reinitialize
    if(http_response.status_code != HTTPStatus.NOT_FOUND):
        return 'Fail4'

    # Get status of the first order
    http_response = requests.get(f"http://localhost:8081/order/1000")

    # If it is not available, then the order request was received before the reinitialize
    if(http_response.status_code == HTTPStatus.NOT_FOUND):

        # Requesting for order
        http_response = requests.post(
            "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})
        
        if(http_response.status_code != HTTPStatus.CREATED):
            return 'Fail5'

        orderId3 = http_response.json().get("orderId")

        # Checks if the starting order id is 1000
        if orderId3 != 1000:
            return 'Fail6'

    elif (http_response.status_code == HTTPStatus.OK):

        # Requesting for order
        http_response = requests.post(
            "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 1})
        
        if(http_response.status_code != HTTPStatus.CREATED):
            return 'Fail7'

        orderId3 = http_response.json().get("orderId")

        # Checks if the current order id is 1001
        # Because the order request given concurrently will have order id 1000 in this cases
        if orderId3 != 1001:
            return 'Fail8'
    
    return 'Pass'


if __name__ == "__main__":

    print(test())