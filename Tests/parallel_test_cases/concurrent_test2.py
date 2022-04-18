from http import HTTPStatus
from threading import Thread
import requests
from time import sleep


# Scenario:
# Check if an agent gets assigned to an order after sign in
# irrespective of whether sign-in request reaches first or the request order request.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def t1(result):  # First concurrent request

    # Signs in agent id 201
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})

    result["1"] = http_response


def t2(result):  # Second concurrent request

    # Requests an order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 3})

    result["2"] = http_response


def test():
    
    Pass='Pass'
    Fail='Fail'

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Checks the status of agent id 201
    http_response = requests.get(
        f"http://localhost:8081/agent/201")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail1'

    res_body = http_response.json()

    status1 = res_body.get("status")

    if status1 != 'signed-out':
        return 'Fail2'


    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###

    # The status codes must be 201 for both the requests.
    if result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED:
        return "Fail3"

    # The orderId returned must be 1000
    orderId1 = result["2"].json().get("orderId")

    if orderId1 != 1000:
        return "Fail4"

    # Loop until the agent gets assigned to the newly created order
    # and the status of both actors change.
    count =0
    while True:
        http_response = requests.get(f"http://localhost:8081/agent/201")
        agent_status = http_response.json().get("status")
        http_response  = requests.get(f"http://localhost:8081/order/1000")
        order_status = http_response.json().get("status")

        if(order_status=="assigned" and agent_status=="unavailable"):
            return Pass
        else:
            # Try for 20 times until a failure occurs.
            if(count==20):
                break
            sleep(2)
        

    return Fail


if __name__ == "__main__":

    print(test())