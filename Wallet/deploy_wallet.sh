docker build -t walletservice .
sudo docker run -v "$(pwd)"/initialData.txt:/initialData.txt -p 8082:8080 --rm --name walletservice-container walletservice &

