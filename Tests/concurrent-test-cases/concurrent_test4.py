from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
# Check if the order delivered remains consistent when given concurrent requests to the same resource.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

def t1(result, id, orderId):  # First concurrent request

    # Changes the status of the given order to delivered
    http_response = requests.post(
        "http://localhost:8081/orderDelivered", json={"orderId": orderId})

    result[id] = http_response

def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")

    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    #Signing in Agent 201
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail1'

    # Checks the status of agent 201
    http_response = requests.get(f"http://localhost:8081/agent/201")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail2'

    res_body = http_response.json()

    status2 = res_body.get("status")

    if (status2 != "available"):
        return "Fail3"

    # Requesting for order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 3})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail4'

    res_body = http_response.json()

    orderId = res_body.get("orderId")

    # Requesting for another order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId":1, "qty": 3})
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail5'

    res_body = http_response.json()

    orderId2 = res_body.get("orderId")

    # Get status of first order
    http_response = requests.get(f"http://localhost:8081/order/{orderId}")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail6'

    res_body = http_response.json()

    agent_id3 = res_body.get("agentId")
    status3 = res_body.get("status")

    if (status3 != "assigned"):
        return "Fail7"

    if (agent_id3 != 201):
        return "Fail8"

    thread = [0 for i in range(10)]

    ### Parallel Execution Begins ###
    for i in range(10):
        thread[i] = Thread(target=t1, kwargs={"result": result, "id": i, "orderId": orderId})

    for i in range(10):
        thread[i].start()

    for i in range(10):
        thread[i].join()

    ### Parallel Execution Ends ###

    for i in range(10):
        if result[i].status_code != HTTPStatus.CREATED:
            return "Fail9"


    # Get status of the second order
    http_response = requests.get(f"http://localhost:8081/order/{orderId2}")

    if(http_response.status_code != HTTPStatus.OK):
        return 'Fail10'

    res_body = http_response.json()

    status4 = res_body.get("status")

    # The status of the second order should be assigned because the first order got delivered
    # and there will be one available agent
    if (status4 != "assigned"):
        return "Fail11"

    if (agent_id3 != 201):
        return "Fail12"
    
    return 'Pass'


if __name__ == "__main__":

    print(test())