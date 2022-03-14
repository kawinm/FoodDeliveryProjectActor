from http import HTTPStatus
from threading import Thread
import requests
from concurrent_helper.concurrent_helper import getAgentStatus
from concurrent_helper.concurrent_helper import getOrderStatus


# Ensure that data present previously is
# reset after reInitialize is called in the delivery service.


# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

def t1(result):
    http_response = requests.post(
        "http://localhost:8081/agentSignIn",
        json={
            "agentId": 202
        }
    )
    result["1"] = http_response

def t2(result):
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

    # Sign in Agent 201
    http_response = requests.post(
        "http://localhost:8081/agentSignIn",
        json={
            "agentId": 201
        }
    )

    ### Parallel Execution Begins ###
    thread1 = Thread(target=t1, kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})

    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###
    # Both agents signin must be successful
    if result["1"].status_code != HTTPStatus.CREATED or result["2"].status_code != HTTPStatus.CREATED:
        return "Fail1"
    
    # The agent 201 must be signed-out
    # The agent 202 must either be signed-out or available
    agent1status = getAgentStatus(201).json()
    agent2status = getAgentStatus(202).json()
    
    if(agent1status.get("status")!="signed-out"):
        return "Fail2"
    
    if(not(agent2status.get("status")== "signed-out" or agent2status.get("status")=="available")):
        return "Fail3"

    return 'Pass'


if __name__ == "__main__":

    print(test())