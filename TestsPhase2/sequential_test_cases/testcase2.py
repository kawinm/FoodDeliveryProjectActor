from http import HTTPStatus
from os import stat
import requests
from time import sleep

#  Check if a customer can make an order to a restaurant
#  after certain amounts of money gets credited to his wallet.

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

    #Get the initial Balance of the customer 301
    http_response = requests.get("http://localhost:8082/balance/301")
    if(http_response.status_code!=HTTPStatus.OK):
        return Fail
    
    balance = http_response.json().get("balance")

    # Deplete the balance of customer 301.
    http_response = requests.post("http://localhost:8082/deductBalance",json={
        "custId" : 301,
        "amount" : balance
    })
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
        return Fail
    
    orderId = http_response.json().get("orderId")
    
    # Retry 5 times until the order status becomes rejected
    # If its not in rejected status return a Fail
    count = 0
    while True:
        http_response = requests.get(f"http://localhost:8081/order/{orderId}")
        status = http_response.json().get("status")
        if(status=="rejected"):
            break
        else:
            if(count==5):
                return Fail
            sleep(2)
            count+=1
    

    # Add some balance to the customer 301
    http_response = requests.post("http://localhost:8082/addBalance",json={
        "custId" : 301,
        "amount" : balance
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        return Fail

    # Let customer 301 make an order with sufficient balance.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 1,
        "qty" : 1
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = "Fail5"
    
    orderId = http_response.json().get("orderId")

    count = 0
    while True:
        http_response = requests.get(f"http://localhost:8081/order/{orderId}")
        status = http_response.json().get("status")
        if(status!="unassigned"):
            return "Fail"
        else:
            if(count == 5):
                return Pass
            sleep(2)
            count+=1
    

    return Pass



if __name__ == "__main__":
    test_result = test()
    print(test_result)
