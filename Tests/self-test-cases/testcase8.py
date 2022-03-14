from http import HTTPStatus
import requests
from helper.helper import *

# Depletes the resources - items and balance
# Reinitialise the service
# Now, checking if the order can be placed 


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

    #Customer 301 makes an order for a total price of Rs.1840
    test_result,order_id1 = createOrderForWallet(301,101,8)
    if(test_result==Fail):
        return Fail

    #Customer 301 again makes an order for a total price of Rs.230, which is more than his current balance
    test_result,order_id2 = createOrderForWallet(301,101,1)
    if(test_result==Fail):
        return Fail

    #Reinitialise the wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail

    #Customer 301 makes an order for 9 quantities of an item and a total price of Rs.1800 
    test_result,order_id1 = createOrderForReinitialise(301,101,9)
    if(test_result==Fail):
        return Fail

    #Customer 301 makes an order for 2 quantities of the same item but only 1 quantity of item remaining 
    test_result,order_id1 = createOrderForReinitialise(302,101,2)
    if(test_result==Fail):
        return Fail

    #Reinitialise the restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail

    #Customer 302 makes an order for 8 quantites of the same item
    test_result,order_id1 = createOrderForReinitialise(302,101,8)
    if(test_result==Fail):
        return Fail

    return Pass

if __name__ == "__main__":
    test_result = test()
    print(test_result)
