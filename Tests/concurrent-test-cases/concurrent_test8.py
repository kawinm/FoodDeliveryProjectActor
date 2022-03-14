from http import HTTPStatus
from threading import Thread
import requests
from concurrent_helper.concurrent_helper import getAgentStatus
from concurrent_helper.concurrent_helper import getOrderStatus

#   Ensure that only one agent is assigned to
#   an order when 2 agents simulatenously sign in


# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

def t1(result):
    http_response = requests.post(
        "http://localhost:8081/agentSignIn",
        json={
            "agentId": 201
        }
    )
    result["1"] = http_response

def t2(result):
    http_response = requests.post(
        "http://localhost:8081/agentSignIn",
       json={
           "agentId" : 202
       }
    )
    result["2"] = http_response

def t3(result):
    http_response = requests.post(
        "http://localhost:8081/requestOrder",
        json={
            "custId":301,
            "restId":101,
            "itemId":1,
            "qty":1
        }
    )
    result["3"] = http_response


def test():

    result = {}

    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")
    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")
    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")


    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})
    thread3 = Thread(target=t3, kwargs={"result": result})

    thread1.start()
    thread2.start()
    thread3.start()

    thread1.join()
    thread2.join()
    thread3.join()

    ### Parallel Execution Ends ###
    # Both agents signin must be successful
    if result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED:
        return "Fail1"
    
    orderstatus = getOrderStatus(1000)
    orderstatus = orderstatus.json()
    if(orderstatus.get("status")!="assigned"):
        return "Fail2"
    
    agent1status = getAgentStatus(201).json()
    agent2status = getAgentStatus(202).json()

    if(not((agent1status.get("status")=="unavailable" or agent2status.get("status")=="unavailable") 
        and (agent1status.get("status")!=  agent2status.get("status")) )):
        return "Fail3"
    

    return 'Pass'


if __name__ == "__main__":

    print(test())