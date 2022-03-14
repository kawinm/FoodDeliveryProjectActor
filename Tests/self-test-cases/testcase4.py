from http import HTTPStatus
import requests
from helper.helper import *

# Check if the status of an unassigned order
# is unassigned even after calling the /orderDelivered endpoint



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

    http_response = requests.post("http://localhost:8081/orderDelivered", json={
        "orderId" : order_id1
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail
    
    status = checkOrderStatus(order_id1,"unassigned")
    if status==False:
        return Fail

    return Pass

if __name__ == "__main__":
    test_result = test()
    print(test_result)
