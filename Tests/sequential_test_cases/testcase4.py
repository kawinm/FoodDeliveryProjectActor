from http import HTTPStatus
import requests
from time import sleep

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
        return Fail
    http_response = requests.post("http://localhost:8081/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail

    #Agent 201 signs in
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 201,
    }) 

    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail

    # Customer 301 makes an order
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 102,
        "itemId" : 1,
        "qty" : 1
    })

    # Wait for the agent status to become unavailable
    while True:
        http_response = requests.get(f"http://localhost:8081/agent/201")
        if(http_response.json().get("status")=="unavailable"):
                break
    # Give an Agent SignOut request
    http_response = requests.post("http://localhost:8081/agentSignOut",json={
        "agentId" : 201
    })

    # Pause for 5 seconds
    sleep(5)
    # Get the status of the agent and make sure its not signed out
    http_response = requests.get(f"http://localhost:8081/agent/201")
    if(http_response.json().get("status")=="unavailable"):
        return Pass
    else:
        return Fail

if __name__ == "__main__":
    test_result = test()
    print(test_result)
