import requests
from http import HTTPStatus
Pass = 'Pass'
Fail = 'Fail'

# Helper function for creating an order
def createOrder(customerid,restid):
    
    test_result = Pass
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : customerid,
        "restId" : restid,
        "itemId" : 2,
        "qty" : 1
    })
    orderid = -1
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
        return(Fail,-1)

    orderid = http_response.json().get("orderId")
    if(orderid==-1):
        return(Fail,-1)
    
    return (test_result,orderid)


# Helper function for checking whether status of order matches with the required status
def checkOrderStatus(orderId,status,order_present=True):
    http_response = requests.get(f"http://localhost:8081/order/{orderId}")
    if order_present:
        if(http_response.status_code != HTTPStatus.OK):
            return False
        ret_status = http_response.json().get("status")
        if(ret_status!=status):
            return False   
    else:
        if(http_response.status_code != HTTPStatus.NOT_FOUND):
            return False    
    return True

# Helper function for checking whether the agent status is the desired or not
def checkAgentStatus(agentId,status):
    http_response = requests.get(f"http://localhost:8081/agent/{agentId}")
    agentStatus = http_response.json().get("status")
    if agentStatus!=status:
        return False
    return True    

def checkOrderAssignedAgent(orderId,agentId):
    http_response = requests.get(f"http://localhost:8081/order/{orderId}")
    if(http_response.status_code!=HTTPStatus.OK):
        return False
    
    agentAssigned = http_response.json().get("agentId")
    if(agentAssigned!=agentId):
        return False
    return True

def createOrderForWallet(customerid,restid, qty):
    
    test_result = Pass
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : customerid,
        "restId" : restid,
        "itemId" : 2,
        "qty" : qty
    })
    orderid = -1
    if(http_response.status_code != HTTPStatus.CREATED and qty == 8):
        test_result = Fail
        return(Fail,-1)

    if(http_response.status_code == HTTPStatus.CREATED and qty == 1):
        test_result = Fail
        return(Fail,-1)

    return (test_result,orderid)

def createOrderForReinitialise(customerid,restid, qty = 1):
    
    test_result = Pass
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : customerid,
        "restId" : restid,
        "itemId" : 1,
        "qty" : qty
    })
    orderid = -1
    if(http_response.status_code != HTTPStatus.CREATED and qty == 9):
        test_result = Fail
        return(Fail,-1)

    if(http_response.status_code == HTTPStatus.CREATED and qty == 2):
        test_result = Fail
        return(Fail,-1)
    
    return (test_result,orderid)

def checkBalance(customerId):
    http_response = requests.get(f"http://localhost:8082/balance/{customerId}")
    if (http_response.status_code != HTTPStatus.OK):
        return (Fail, -1)

    balance = http_response.json().get("balance")    
    return (Pass, balance)

def agentSignIn(agentId):
    http_response = requests.post("http://localhost:8081/agentSignIn",json={
        "agentId" : agentId,
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
        return Fail
    return Pass

def agentSignOut(agentId):
    http_response = requests.post("http://localhost:8081/agentSignOut",json={
        "agentId" : agentId,
    })
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
        return Fail
    return Pass

def createOrderForDepletion(customerid,restid, qty = 1):
    
    test_result = Pass
    http_response = requests.post("http://localhost:8081/requestOrder",json={
        "custId" : customerid,
        "restId" : restid,
        "itemId" : 1,
        "qty" : qty
    })
    orderid = -1
    if(http_response.status_code != HTTPStatus.CREATED and qty == 10):
        test_result = Fail
        return(Fail,-1)

    if(http_response.status_code == HTTPStatus.CREATED and qty == 2):
        test_result = Fail
        return(Fail,-1)

    orderid = http_response.json().get("orderId")
    if(orderid==-1):
        return(Fail,-1)
    
    return (test_result,orderid)

def refillItem(restid, itemid, qty):
    
    test_result = Pass
    http_response = requests.post("http://localhost:8080/refillItem",json={
        "restId" : restid,
        "itemId" : itemid,
        "qty" : qty
    })

    orderid = -1
    if(http_response.status_code != HTTPStatus.CREATED):
        test_result = Fail
        return Fail
    
    return Pass
