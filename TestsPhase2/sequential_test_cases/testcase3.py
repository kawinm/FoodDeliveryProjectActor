from collections import defaultdict
from http import HTTPStatus
import http
from os import stat
import requests

# Check if only one agent gets assigned to an order when there is one pending order
# and there are two agents which are available

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

    # Let customer 301 make an order with insufficient balance.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 1,
        "qty" : 1
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail1"
    
    orderId = http_response.json().get("orderId")

    # Agent 201 Signs In
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 201,
    }) 
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail2"

    # Agent 202 signs in
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : 202,
    }) 
    if(http_response.status_code != HTTPStatus.CREATED):
        return "Fail3"

    # Iterate through the loop until the order gets assigned an agent.
    while True:
        http_response = requests.get(f"http://localhost:8081/order/{orderId}")
        status = http_response.json().get("status")
        if(status!="unassigned" and status!="assigned"):
            return "Fail4"
        if(status=="assigned"):
            break
    
    agent_statuses_count = defaultdict(lambda: 0)
    http_response = requests.get(f"http://localhost:8081/agent/201")
    status = http_response.json().get("status")
    agent_statuses_count[status]+=1

    http_response = requests.get(f"http://localhost:8081/agent/202")
    status = http_response.json().get("status")
    agent_statuses_count[status]+=1

    if(agent_statuses_count["unavailable"]==1 and agent_statuses_count["available"]==1):
        return Pass
    else:
        return "Fail5"


if __name__ == "__main__":
    test_result = test()
    print(test_result)
