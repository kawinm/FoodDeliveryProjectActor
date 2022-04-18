from collections import defaultdict
from http import HTTPStatus
from os import stat
from threading import Thread
import requests


# Scenario:
#   Check if a parallel sequence of 5 requests
#   3 requestOrders and 2 AgentSignIn Requests are consistent.
#   One of 3 orders will remain unassigned while the others are assigned 
#   agents.

# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082


# Customer 301 making an order request.
def t1(result):  
    # Customer 301 makes an order 
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId":101, "itemId":1, "qty": 1})

    result["1"] = http_response


# Customer 302 making an order request.
def t2(result):  
    # Customer 302 makes an order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 302, "restId":101, "itemId":1, "qty": 1})

    result["2"] = http_response

# Customer 301 making an order request.
def t3(result):  
    # Customer 301 makes an order
    http_response = requests.post(
        "http://localhost:8081/requestOrder", json={"custId": 301, "restId":102, "itemId":1, "qty": 1})

    result["3"] = http_response

# Agent 201 signing in
def t4(result):
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 201,
    }) 
    result["4"] = http_response

# Agent 202 signing in
def t5(result):
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 202,
    }) 
    result["5"] = http_response


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
    thread5 = Thread(target=t5, kwargs={"result": result})



    thread1.start()
    thread2.start()
    thread3.start()
    thread4.start()
    thread5.start()

    thread1.join()
    thread2.join()
    thread3.join()
    thread4.join()
    thread5.join()


    ### Parallel Execution Ends ###

    if (result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED 
        or result["3"].status_code != HTTPStatus.CREATED or result["4"].status_code != HTTPStatus.CREATED 
        or result["5"].status_code != HTTPStatus.CREATED
        ) :
        return "Fail2"

    
    # Continue until all the agents are unavailable
    while True:
        http_response = requests.get(f"http://localhost:8081/agent/201")
        agent201_status = http_response.json().get("status")
        http_response = requests.get(f"http://localhost:8081/agent/202")
        agent202_status = http_response.json().get("status")
        
        if(agent201_status==agent202_status and agent201_status=="unavailable"):
                break
    
    # Check all orderstatuses 
    # Count the number of orders belonging to a particular order status.
    # only 2 should ideally be assigned while the remaining one unassigned
    orderstatuses = defaultdict(lambda: 0)
    http_response = requests.get(f"http://localhost:8081/order/1000")
    status = http_response.json().get("status")
    orderstatuses[status] +=1
    http_response = requests.get(f"http://localhost:8081/order/1001")
    status = http_response.json().get("status")
    orderstatuses[status] +=1
    http_response = requests.get(f"http://localhost:8081/order/1002")
    status = http_response.json().get("status")
    orderstatuses[status]+=1

    if(orderstatuses["assigned"]==2 and orderstatuses["unassigned"]==1):
        return Pass
    else:
        return Fail
    return 'Pass'


if __name__ == "__main__":

    print(test())