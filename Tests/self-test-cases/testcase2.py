from http import HTTPStatus
import requests
import random
from helper.helper import *

# Check if the next agent having the lowest id is assigned to an order 
# after an agent signs out while orders are coming


Pass = 'Pass'
Fail = 'Fail'

def test():
    test_result = Pass

    '''
        Reinitialize all the servies.
    '''
    http_response = requests.post("http://localhost:8080/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    http_response = requests.post("http://localhost:8081/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail

    agents = [201,202,203]
    random.shuffle(agents)  # Shuffle the signin order of  agents

    for agent in agents:
        http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": agent})

        result = checkAgentStatus(agent,"available")
        if result==False:
            return Fail
    
    # Customer 301 makes an order
    test_result,order_id1 = createOrder(301,101)
    if(test_result==Fail):
        return Fail
  
    # Order made by Customer 301 must be assigned as there are free agents
    status = checkOrderStatus(order_id1,"assigned")
    if status==False:
        return Fail

    # Check if Agent 201 is the agent assigned to the order made by 301
    status = checkOrderAssignedAgent(order_id1,201)
    if status == False:
        return Fail

    # Agent 202 signs out
    http_response = requests.post(
        "http://localhost:8081/agentSignOut", json={"agentId": 202})
    
    # Customer 302 makes an order 
    test_result,order_id2 = createOrder(302,101)
    if(test_result==Fail):
        return Fail

    # Check if the order made by customer 302 is assigned
    status = checkOrderStatus(order_id2,"assigned")
    if status==False:
        return Fail   

    # Check if agent 202 is not assigned to the new order and is signed out
    status = checkAgentStatus(202,"signed-out")
    if status == False:
        return Fail
    
    # Check if the next available agent according to agentId is 
    # assigned to the new order.
    status = checkOrderAssignedAgent(order_id2,203)
    if status == False:
        return Fail

    return test_result


if __name__ == "__main__":
    test_result = test()
    print(test_result)
