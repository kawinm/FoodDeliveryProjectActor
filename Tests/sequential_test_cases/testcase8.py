from collections import defaultdict
from http import HTTPStatus
import requests
from time import sleep

# Tries to Signout an Assigned Agent and
# Check if his status is still Unavailable

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

    # Agent 202 signs in
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 202,
    }) 
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail

    #Customer 301 makes an order
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 1,
        "qty" : 1
    })

    # Check status
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail
    
    # Check the orderId
    orderId1 = http_response.json().get("orderId")
    if(orderId1!=1000):
        return Fail

    # Customer 302 makes an order
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 302,
        "restId" : 101,
        "itemId" : 1,
        "qty" : 1
    })

    # Check status of the request.
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail

    # Check the orderId
    orderId2 = http_response.json().get("orderId")
    if(orderId2 != 1001):
        return Fail

    # Customer 301 makes an order
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 2,
        "qty" : 1
    })

    # Check the status of the request
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail
    
    # Check the orderId 
    orderId3 = http_response.json().get("orderId")
    if(orderId3 != 1002):
        return Fail

    orders = [orderId1,orderId2,orderId3]
    
    # Loop through all the agents until each gets assigned to an order.
    agents=[201,202]
    while True:
        count = 0
        for agent in agents:
            http_response = requests.get(f"http://localhost:8081/agent/{agent}")
            if(http_response.json().get("status")=="unavailable"):
                count+=1
        if(count==2):
            break
    

    # There should be 1 order waiting for requests and not any other number
    d = defaultdict(default_value)
    for order in orders:
        http_response = requests.get(f"http://localhost:8081/order/{order}")
        status = http_response.json().get("status")
        d[status] +=1
    
    # Check if only one order is left unassigned.
    if(d["unassigned"]!=1):
        return Fail

    # Signout Agent 201
    http_response = requests.post("http://localhost:8081/agentSignOut",json={
        "agentId" : 201
    })

    http_response = requests.get(f"http://localhost:8081/agent/201")
    if(http_response.json().get("status")!="unavailable"):
        return Fail

    return Pass
if __name__ == "__main__":
    test_result = test()
    print(test_result)
