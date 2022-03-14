from http import HTTPStatus
import requests
from helper.helper import *

# Check if the highest numbered order id is left
# when there are 3 pending orders and 2 agents sign in
# Assuming sufficient balance for all customers


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

    # Customer 302 makes an order
    test_result,order_id2 = createOrder(302,101)
    if(test_result==Fail):
        return Fail

    #Check the order created by 302 is unassigned
    status = checkOrderStatus(order_id2,"unassigned")
    if status==False:
        return Fail
    

    # Customer 303 makes an order
    test_result,order_id3 = createOrder(303,101)
    if(test_result==Fail):
        return Fail

    #Check the order created by 303 is unassigned
    
    status = checkOrderStatus(order_id3,"unassigned")
    if status==False:
        return Fail

    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 201})

    result = checkAgentStatus(201,"unavailable")
    if result==False:
        return Fail

    http_response = requests.post(
        "http://localhost:8081/agentSignIn", json={"agentId": 202})
    if result==False:
        return Fail

    status = checkOrderStatus(order_id1,"assigned")
    if status==False:
        return Fail
    
    status = checkOrderStatus(order_id2,"assigned")
    if status==False:
        return Fail

    status = checkOrderStatus(order_id3,"unassigned")
    if status==False:
        return Fail

    return test_result


if __name__ == "__main__":
    test_result = test()
    print(test_result)
