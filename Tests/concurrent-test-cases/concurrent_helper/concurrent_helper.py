from http import HTTPStatus
from threading import Thread
import requests

def getOrderStatus(orderId):
    order_url = "http://localhost:8081/order/"+str(orderId)
    http_response = requests.get(order_url)
    return http_response

def getAgentStatus(agentId):
    agent_url = "http://localhost:8081/agent/"+str(agentId)
    http_response = requests.get(agent_url)
    return http_response