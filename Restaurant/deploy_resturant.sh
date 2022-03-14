docker build -t resturantservice .
sudo docker run -v "$(pwd)"/initialData.txt:/initialData.txt -p 8080:8080 --rm --name restaurantservice-container restaurantservice &

