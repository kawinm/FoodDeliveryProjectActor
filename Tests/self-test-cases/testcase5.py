from http import HTTPStatus
import requests
from helper.helper import *

# Check if the agentId field in JSON response of OrderStatus is correct in the scenarios
# 1. An agent has not been assigned to an order
# 2. The available agent having lowest id is assigned to the order.

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

    #Customer 301 makes an order
    test_result,order_id1 = createOrder(301,101)
    if(test_result==Fail):
        return Fail
    
    #Check the created order is unassigned
    status = checkOrderStatus(order_id1,"unassigned")
    if status==False:
        return Fail

    # Check if the agentId field is -1 for an unassigned order
    status = checkOrderAssignedAgent(order_id1,-1)
    if status  == False:
        return Fail
    

    agent1 = 201
    agent2 = 202
    # Agent 201 signs in 
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": agent2})

    #Agent 202 signs in
    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": agent1})
    
    # Check the status of the order must be assigned as agents signed in
    status = checkOrderStatus(order_id1,"assigned")
    if status==False:
        return Fail
    
    # The agent assigned to the new order must be 202 as he signed in first
    status = checkOrderAssignedAgent(order_id1,agent2)
    if status  == False:
        return Fail
    
    # The status of 201 must be available
    status = checkAgentStatus(agent1,"available")
    if status == False:
        return Fail

    return test_result


if __name__ == "__main__":
    test_result = test()
    print(test_result)
