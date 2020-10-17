# P2P System
> This is an implementation of a peer-to-peer system where the clients can share files with the help of a tracker.

## Prerequisites
* libsqlite3-dev: sudo apt-get install libsqlite3-dev
* Sun RPC: sudo apt-get install rpcbind
* Java 8 and JDK
* gcc
* make


## Installation
Clone the repository
```bash
git clone https://github.com/adrihacar/p2p-system.git
```
Run **make** to compile the project
```bash
cd .p2p-system 
make
```

## Usage
Init the tracker specifying the port that it will listen on
```bash
./server -p 1234
```
Run the publusher and and as many clients as you want specifiying the domain or IP address of the tracker and the port 
```bash
java -cp jaxws-ri/lib/*:. upper/UpperPublisher
java -cp jaxws-ri/lib/*:. Client -s <tracker ip/domain> -p <port>
```
Once at the Client shell you should see a prompt **C>**. There you can type the commands: CONNECT, DISCONNECT, GET_FILE, REGISTER, UNREGISTER, PUBLISH, DELETE, LIST_USERS, LIST_CONTENT, QUIT
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the GPL-3.0 License. See `LICENSE` for more information.

## Contact

Adrián Hacar Sobrino - [@adrihacar](https://twitter.com/adrihacar
