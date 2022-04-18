from http import HTTPStatus
from threading import Thread
import requests
from time import sleep


# Check if an availble agent changes status when 
# a concurrent requestOrder and agentSignout comes

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


def t1(result):  
    # Customer 301 requests an order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId": 101, "itemId": 1, "qty": 3})

    result["1"] = http_response


def t2(result):  
    # SignOut request for Agent 201 
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId": 201})

    result["2"] = http_response


def test():

    result = {}
    Pass='Pass'
    Fail='Fail'

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")
    if(http_response.status_code!=HTTPStatus.CREATED):
        return Fail
    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")
    if(http_response.status_code!=HTTPStatus.CREATED):
        return Fail

    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code!=HTTPStatus.CREATED):
        return Fail

    # Agent 201 sign in
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})

    if(http_response.status_code != HTTPStatus.CREATED):
        return 'Fail1'

    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###
    # Sleep for some time so that the effects are propogated.
    sleep(7)

    # The status of the agent must be either signed-out or unavailable
    http_response = requests.get("http://localhost:8081/agent/201")
    status = http_response.json().get("status")
    if(status!= "unavailable" and status!="signed-out"):
        return "Fail4"
    
    # If the status of the agent is unavailable check if he is assigned to
    # the latest order.
    if(status=="unavailable"):
        http_response = requests.get("http://localhost:8081/order/1001")
        order_status = http_response.json().get("status")
        agent_alloted = http_response.json().get("agentId")
        if(order_status!="assigned" or agent_alloted != 201):
            return "Fail5"
    
    return 'Pass'


if __name__ == "__main__":

    print(test())