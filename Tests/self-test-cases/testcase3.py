from http import HTTPStatus
import requests

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
        test_result = Fail
    http_response = requests.post("http://localhost:8081/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    http_response = requests.post("http://localhost:8082/reInitialize")
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail

    #Get the initial Balance of the customer 301
    http_response = requests.get("http://localhost:8082/balance/301")
    if(http_response.status_code!=HTTPStatus.OK):
        test_result = Fail
    balance = http_response.json().get("balance")

    # Deplete the balance of customer 301.
    http_response = requests.post("http://localhost:8082/deductBalance",json={
        "custId" : 301,
        "amount" : balance
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    

    # Let customer 301 make an order with insufficient balance.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 1,
        "qty" : 1
    })
    if(http_response.status_code != HTTPStatus.GONE):
        test_result = Fail
    
    # Add some balance to the customer 301
    http_response = requests.post("http://localhost:8082/addBalance",json={
        "custId" : 301,
        "amount" : balance
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail

    # Let customer 301 make an order with sufficient balance.
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : 301,
        "restId" : 101,
        "itemId" : 1,
        "qty" : 1
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
    

    return test_result


if __name__ == "__main__":
    test_result = test()
    print(test_result)
