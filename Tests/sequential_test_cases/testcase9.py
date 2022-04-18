from collections import defaultdict
from http import HTTPStatus
import requests
from time import sleep

# Tries to Signout an Available Agent and
# Check if his status changes to Signed-out

Pass = 'Pass'
Fail = 'Fail'

def default_value():
    return 0

def test():
    test_result = Pass

    '''
        Reinitialize all the servies.
    '''
    http_response = requests.post("http://localhost:8080/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail
    http_response = requests.post("http://localhost:8081/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail

    
    
    # Agent 201 Signs In
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 201,
    }) 
    
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail


    # Signout Agent 201
    http_response = requests.post("http://localhost:8081/agentSignOut",json={
        "agentId" : 201
    })

    http_response = requests.get(f"http://localhost:8081/agent/201")
    if(http_response.json().get("status")!="signed-out"):
        return Fail

    return Pass
if __name__ == "__main__":
    test_result = test()
    print(test_result)
