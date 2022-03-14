from http import HTTPStatus
from threading import Thread
import requests

# Scenario:
# Check if only one customer is able to deplete all quantites of a given item
# when parallel order requests come for the same item


# RESTAURANT SERVICE    : http://localhost:8080
# DELIVERY SERVICE      : http://localhost:8081
# WALLET SERVICE        : http://localhost:8082

# Request made by the customer with customer id 301
def t1(result):
    http_response = requests.post(
        "http://localhost:8081/requestOrder",
        json={
            "custId": 301,
            "restId": 101,
            "itemId" : 1,
            "qty" : 10
        }
    )
    result["1"] = http_response

# Request made by the customer with id 302
def t2(result):
    http_response = requests.post(
        "http://localhost:8081/requestOrder",
        json={
            "custId": 302,
            "restId": 101,
            "itemId" : 1,
            "qty" : 10
        }
    )
    result["2"] = http_response


def test():
    # Reinitialize Restaurant service
    http_response = requests.post("http://localhost:8080/reInitialize")
    # Reinitialize Delivery service
    http_response = requests.post("http://localhost:8081/reInitialize")
    # Reinitialize Wallet service
    http_response = requests.post("http://localhost:8082/reInitialize")

    # Get the Initial balances of the customers
    cust301_initial_balance = 0
    cust302_initial_balance = 0
    cust301_initial_balance = requests.get("http://localhost:8082/balance/301").json().get("balance")
    cust302_initial_balance = requests.get("http://localhost:8082/balance/302").json().get("balance")

    ### Parallel Execution Begins ##
    result = {}
    thread1 = Thread(target=t1 ,kwargs={"result": result})
    thread2 = Thread(target=t2, kwargs={"result": result})
    
    thread1.start()
    thread2.start()

    thread1.join()
    thread2.join()

    ### Parallel Execution Ends ###

    # One of the orders must be created
    if ((result["1"].status_code == HTTPStatus.CREATED and result["2"].status_code == HTTPStatus.CREATED)  
        or (result["1"].status_code == HTTPStatus.GONE and result["2"].status_code == HTTPStatus.GONE)):
        return "Fail1"

    cust301_final_balance = requests.get("http://localhost:8082/balance/301").json().get("balance")
    cust302_final_balance = requests.get("http://localhost:8082/balance/302").json().get("balance")

    # Check if the wallet balance is restored for the customers whose order request failed
    if(result["1"].status_code == HTTPStatus.GONE):
        if(cust301_final_balance!=cust301_initial_balance):
            return "Fail2"
    
    if(result["2"].status_code == HTTPStatus.GONE):
        if(cust302_final_balance!=cust302_initial_balance):
            return "Fail3"

    return 'Pass'


if __name__ == "__main__":

    print(test())