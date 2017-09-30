import boto3
# Get the service resource
sqs = boto3.resource('sqs')
# Get the queue
queue = sqs.get_queue_by_name(QueueName='Wifi_Indoor_Positioning')
queue_url = 'https://cn-north-1.queue.amazonaws.com.cn/444376591338/Wifi_Indoor_Positioning'
outputfile = open('D:/draw/Test4.txt','a')

#Send Messages
#response = queue.send_message(MessageBody='hello')
#print(response.get('MessageId'))
#print(response.get('MD5OfMessageBody'))
#response = queue.send_message(MessageBody='world')
#print(response.get('MessageId'))
#print(response.get('MD5OfMessageBody'))

# Process messages by printing out body and optional author name
while 1:
    a = 0
    b = 0
    for message in queue.receive_messages(MessageAttributeNames=['Author']):
        # Get the custom author message attribute if it was set
        author_text = ''
        if message.message_attributes is not None:
            author_name = message.message_attributes.get('Author').get('StringValue')
            if author_name:
                author_text = ' ({0})'.format(author_name)
        
                # Print out the body and author (if set)
        print('{0}\n{1}'.format(message.body, author_text))
        outputfile.write(message.body+'\n');
        a = a + 1
            
                # Let the queue know that the message is processed
        message.delete()
    if (b == a):
        break;
    b = a
outputfile.close();