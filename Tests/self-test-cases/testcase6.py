from http import HTTPStatus
import requests
from helper.helper import *

# Depleting a customer's wallet balance and
# checking if the next order by the same customer is getting rejected or not


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

    #Checks the initial balance of Customer 301's wallet
    test_result, initial_balance = checkBalance(301)
    if(test_result==Fail):
        return Fail


    #Customer 301 makes an order for a total price of Rs.1840
    test_result,order_id1 = createOrderForWallet(301,101,8)
    if(test_result==Fail):
        return Fail

    #Checks the balance of Customer 301's wallet after the order is placed
    test_result, balance = checkBalance(301)
    if(test_result==Fail or (initial_balance - balance) != 1840):
        return Fail

    #Customer 301 again makes an order for a total price of Rs.230
    test_result,order_id2 = createOrderForWallet(301,101,1)
    if(test_result==Fail):
        return Fail

    #Checks the balance of Customer 301's wallet to see if there is any change in balance
    test_result, post_balance = checkBalance(301)
    if(test_result==Fail or balance != post_balance):
        return Fail    
    return test_result


if __name__ == "__main__":
    test_result = test()
    print(test_result)
