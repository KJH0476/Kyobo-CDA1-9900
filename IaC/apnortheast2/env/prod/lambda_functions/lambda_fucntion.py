import json
import os
import logging
import time
import urllib.request
import urllib.error
import base64
import boto3
from datetime import datetime
from requests_aws4auth import AWS4Auth
from requests.auth import HTTPBasicAuth
from opensearchpy import OpenSearch, RequestsHttpConnection

logger = logging.getLogger()
logger.setLevel(logging.INFO)

OPENSEARCH_ENDPOINT = os.getenv('OPENSEARCH_ENDPOINT')
REGION = os.getenv('OPENSEARCH_REGION')
INDEX_NAME = os.getenv('OPENSEARCH_INDEX')

# AWS 자격 증명을 가져와 OpenSearch에 대한 AWS4 인증 객체 생성
credentials = boto3.Session(region_name=REGION).get_credentials()
awsauth = AWS4Auth(credentials.access_key, credentials.secret_key, REGION, 'es', session_token=credentials.token)

# OpenSearch 클라이언트 초기화
search_client = OpenSearch(
    hosts=[{'host': OPENSEARCH_ENDPOINT, 'port': 443}],
    http_auth=awsauth,
    use_ssl=True,
    verify_certs=True,
    connection_class=RequestsHttpConnection
)

def lambda_handler(event, context):
    """
    AWS Lambda의 메인 핸들러 함수
    DynamoDB 스트림 이벤트를 처리하여 OpenSearch에 데이터를 삽입, 수정 또는 삭제 처리

    @param event: Lambda 함수로 전달된 이벤트 데이터
    @param context: Lambda 함수 실행 환경에 대한 정보
    """
    try:
        for record in event['Records']:
            try:
                event_name = record['eventName']
                # 스트림에서 새로 삽입된, 수정된, 삭제된 데이터를 처리
                if event_name == 'INSERT':
                    handle_insert(record)
                elif event_name == 'MODIFY':
                    handle_modify(record)
                elif event_name == 'REMOVE':
                    handle_remove(record)
                else:
                    logger.warning(f"Unhandled event name: {event_name}")
            except Exception as e:
                logger.error(f"Error processing record {record}: {e}")
    except Exception as e:
        logger.error(f"Error in lambda_handler: {e}")


def handle_insert(record):
    try:
        new_data = record['dynamodb']['NewImage']
        document = format_data(new_data)
        send_to_opensearch(document, action='insert')
    except Exception as e:
        logger.error(f"Error in handle_insert for record {record}: {e}")


def handle_modify(record):
    """
    DynamoDB 스트림에서 수정된 데이터를 처리하여 OpenSearch에 데이터를 수정

    @param record: DynamoDB 스트림에서 수정된 데이터를 포함하는 레코드
    """
    try:
        new_data = record['dynamodb']['NewImage']
        document = format_data(new_data)
        send_to_opensearch(document, action='modify')
    except Exception as e:
        logger.error(f"Error in handle_modify for record {record}: {e}")


def handle_remove(record):
    """
    DynamoDB 스트림에서 삭제된 데이터를 처리하여 OpenSearch에 데이터를 삭제

    @param record: DynamoDB 스트림에서 삭제된 데이터를 포함하는 레코드
    """
    try:
        old_data = record['dynamodb']['OldImage']
        document_id = old_data['restuarant_id']['S']
        send_to_opensearch({'id': document_id}, action='delete')
    except Exception as e:
        logger.error(f"Error in handle_remove for record {record}: {e}")


def format_data(data):
    """
    DynamoDB 스트림에서 새로 삽입된 데이터를 OpenSearch에 맞는 형식으로 변환

    @param data: DynamoDB 스트림에서 새로 삽입된 데이터
    """
    try:
        logger.info(data)
        # DynamoDB의 데이터 형식을 OpenSearch에 맞는 형식으로 변환
        return {
            'id': data['restuarant_id']['S'],
            'restaurant_name': data['restaurant_name']['S'],
            'address': data['address']['S'],
            'food_type': data['food_type']['S'],
            'latitude': float(data['latitude']['N']),
            'longitude': float(data['longitude']['N']),
            'menu': [
                {
                    'menu_name': name,
                    'menu_price': float(menu_price),
                    'image_url': image_url
                }
                for name, menu_price, image_url in zip(
                    data['menu']['M']['menu_name']['SS'],
                    data['menu']['M']['menu_price']['NS'],
                    data['menu']['M']['image_url']['SS']
                )
            ],
            'phone_number': data['phone_number']['S'],
            'update_at': int(time.time() * 1000),
            'timestamp': datetime.utcnow().isoformat()
        }
    except Exception as e:
        logger.error(f"Error in format_data with data {data}: {e}")
        raise


def send_to_opensearch(document, action):
    """
    변환된 데이터를 OpenSearch에 삽입, 수정 또는 삭제

    @param document: OpenSearch에 삽입할 데이터
    @param action: 수행할 작업 (insert, modify, delete)
    """
    logger.info(f"Send To OpenSearch: Action={action}, Document ID={document.get('id')}")
    try:
        if action in ['insert', 'modify']:
            response = search_client.index(
                index=INDEX_NAME,
                id=document['id'],
                body=document
            )
            logger.info(f"Successfully {action}d document ID {document['id']}: {response}")
        elif action == 'delete':
            response = search_client.delete(
                index=INDEX_NAME,
                id=document['id']
            )
            logger.info(f"Successfully deleted document ID {document['id']}: {response}")
        else:
            logger.error(f"Unknown action: {action}")
    except Exception as e:
        logger.error(f"Error processing {action} for document ID {document.get('id', 'N/A')}: {e}")
        raise