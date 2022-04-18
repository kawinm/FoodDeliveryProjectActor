from ast import Return
from collections import defaultdict
from http import HTTPStatus
from os import stat
from threading import Thread
import requests
from time import sleep


# Scenario:
# Check Agent Status when an OrderDelivered Request and AgentSignOut Request come

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082



def t1(result):  
    # Agent 201 signs out
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId": 201})

    result["1"] = http_response


# Order with order id 1000 gets delivered
def t2(result):
    # The order with orderId 1000 gets delivered
    http_response = requests.post(
        "http://localhost:8081/orderDelivered", json={"orderId": 1000}
    )
    result["2"] = http_response




def test():

    result = {}
    Pass = 'Pass'
    Fail = 'Fail'
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


    # Customer 301 makes an order request
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId":101, "itemId":1, "qty": 1})
    if (http_response.status_code!=HTTPStatus.CREATED):
        return Fail
    
    # Agent 201 signs in
    http_response = requests.post(
        "http://localhost:8081/agentSignIn",json={"agentId":201}
    )
    if(http_response.status_code!=HTTPStatus.CREATED):
        return Fail
    
    # Continue until the agents gets assigned to the order with id 1000
    while True:
        http_response = requests.get("http://localhost:8081/agent/201")
        status = http_response.json().get("status")
        if(status=="unavailable"):
            break
    
    http_response = requests.get("http://localhost:8081/order/1000")
    agentAssigned = http_response.json().get("agentId")
    if(agentAssigned!=201):
        return Fail


    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###
    # Sleep for some moments so that effects are propogated properly
    sleep(7)
    # Check the status codes of requests.
    if (result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED ) :
        return "Fail4"

    
    # Get the status of the order with id 1000
    http_response = requests.get("http://localhost:8081/order/1000")
    status = http_response.json().get("status")
    if(status!="delivered"):
        return "Fail5"
    
    # Get status of Agent 201
    http_response = requests.get("http://localhost:8081/agent/201")
    status = http_response.json().get("status")
    
    # The Agent must be either signedout or available.
    if(status!='signed-out' and status != 'available'):
        return "Fail6"
    
    return Pass
    


if __name__ == "__main__":

    print(test())