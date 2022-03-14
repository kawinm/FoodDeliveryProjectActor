from http import HTTPStatus
import requests
from helper.helper import *

# Assuming two agents are in signed-in state
# When an order is requested by a customer
# Making sure that the agent with lowest agent id is assigned to the order


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

    #Agent 201 signs in
    test_result = agentSignIn(201)
    if(test_result==Fail):
        return Fail

    #Agent 202 signs in
    test_result = agentSignIn(202)
    if(test_result==Fail):
        return Fail

    #Customer 301 requests an order
    test_result,order_id1 = createOrder(301,101)
    if(test_result==Fail):
        return Fail

    # Check if agent 201 is assigned to the new order
    status = checkAgentStatus(201,"unavailable")
    if status == False:
        return Fail

    # Check if agent 202 is not assigned to the new order
    status = checkAgentStatus(202,"available")
    if status == False:
        return Fail

    return Pass

if __name__ == "__main__":
    test_result = test()
    print(test_result)
