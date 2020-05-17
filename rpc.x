
struct user {
    string name<256>;
    string ip <20>;
    string port <8>:
}

program PROG{
    version PROGVER{
        void init_database() = 1;
        char my_register(string user_name<256>) = 2;
        char unregister(string user_name<256>) = 3;
        char publish(string user_name<256>, string file_name<256>,string file_description<256>) = 4;
        char my_delete(string user_name<256>, string file_name<256>) = 5;
        char my_connect(string user_name<256>, string clientip<20>,string client_port<8>) = 6;
        char disconnect(string user_name<256>) = 7;
    } = 1;
} = 99;
