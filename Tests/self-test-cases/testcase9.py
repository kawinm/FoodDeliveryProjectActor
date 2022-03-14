from http import HTTPStatus
import requests
from helper.helper import *

# Agent is out for delivery (i.e at unavailable state)
# Agent tries to sign out
# Checks the status of the agent


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

    #Customer 301 places an order for a total price of Rs.1840
    test_result,order_id1 = createOrderForWallet(301,101,8)
    if(test_result==Fail):
        return Fail

    #Agent 201 tries to sign out
    test_result = agentSignOut(201)
    if(test_result==Fail):
        return Fail

    # Check if agent 201 is still at unavailable state
    status = checkAgentStatus(201,"unavailable")
    if status == False:
        return Fail

    return Pass

if __name__ == "__main__":
    test_result = test()
    print(test_result)
