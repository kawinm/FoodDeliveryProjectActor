from collections import defaultdict
from http import HTTPStatus
from os import stat
from threading import Thread
import requests
from time import sleep


# Scenario:
#   Check if an order request which ought to get rejected doesnt get an agent.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


# Customer 301 making an order request with insufficient balance
def t1(result):  
    # Customer 301 makes an order 
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId":101, "itemId":2, "qty": 11})

    result["1"] = http_response


# Agent 201 signs in 
def t2(result):  
    # Agent 201 signs in
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})

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

    
    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###

    if (result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED ) :
        return Fail

    sleep(7)
    
    # Get status of Agent 201
    http_response = requests.get("http://localhost:8081/agent/201")
    status = http_response.json().get("status")
    if(status!="available"):
        return Fail
    
    # The status of the order must be rejected.
    http_response = requests.get("http://localhost:8081/order/1000")
    status = http_response.json().get("status")
    if(status != "rejected"):
        return Fail


    return Pass
    


if __name__ == "__main__":

    print(test())