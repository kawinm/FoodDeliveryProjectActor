from collections import defaultdict
from http import HTTPStatus
from os import stat
from threading import Thread
import requests


# Scenario:
#   Check if only one agent gets an order   
#   when an order and 3 agents sign in simultaneously.


# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


# Customer 301 making an order request.
def t1(result):  
    # Customer 301 makes an order 
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId":101, "itemId":1, "qty": 1})

    result["1"] = http_response


# Agent 201 signing in
def t2(result):
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 201,
    }) 
    result["2"] = http_response

# Agent 202 signing in
def t3(result):
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 202,
    }) 
    result["3"] = http_response


# Agent 203 signing in
def t4(result):
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 203,
    }) 
    result["4"] = http_response


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
    thread3 = Thread(target=t3, kwargs={"result": result})
    thread4 = Thread(target=t4, kwargs={"result": result})

    thread1.start()
    thread2.start()
    thread3.start()
    thread4.start()

    thread1.join()
    thread2.join()
    thread3.join()
    thread4.join()


    ### Parallel Execution Ends ###

    if (result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED 
        or result["3"].status_code != HTTPStatus.CREATED or result["4"].status_code != HTTPStatus.CREATED 
        ) :
        return Fail

    
    # Continue until all the order gets an agent assigned
    while True:
        http_response = requests.get(f"http://localhost:8081/order/1000")
        status = http_response.json().get("status")
        if(status=="assigned"):
            break
        
        
    agent_statues = defaultdict(lambda:0)  
    http_response = requests.get(f"http://localhost:8081/agent/201")
    status = http_response.json().get("status")
    agent_statues[status] +=1

    http_response = requests.get(f"http://localhost:8081/agent/202")
    status = http_response.json().get("status")
    agent_statues[status] +=1

    http_response = requests.get(f"http://localhost:8081/agent/203")
    status = http_response.json().get("status")
    agent_statues[status] +=1

    if(agent_statues["available"]==2 and agent_statues["unavailable"]==1):
        return Pass
    else:
        return Fail

if __name__ == "__main__":

    print(test())