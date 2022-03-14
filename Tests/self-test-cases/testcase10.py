from http import HTTPStatus
import requests
from helper.helper import *

# A customer depleted an item stock
# Another customer placed an order for the same item
# Refill the item
# Check if the customer can place an order for this item now


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

    #Customer 301 makes an order for a total price of Rs.1800 and depletes the item completely
    test_result,order_id1 = createOrderForDepletion(301,101,10)
    if(test_result==Fail):
        return Fail

    #Customer 302 makes the order for the depleted item
    test_result,order_id2 = createOrderForWallet(302,101,2)
    if(test_result==Fail):
        return Fail

    #Refill 2 quantities of the item 
    test_result = refillItem(101,1,2)
    if(test_result==Fail):
        return Fail

    #Customer 302 again makes an order for 2 quantities of the item
    test_result,order_id2 = createOrderForWallet(302,101,2)
    if(test_result==Fail):
        return Fail

    return Pass

if __name__ == "__main__":
    test_result = test()
    print(test_result)
