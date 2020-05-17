
program PROG{
    version PROGVER{
        char my_register(string user_name<256>) = 1;
        char unregister(string user_name<256>) = 2;
        char publish(string user_name<256>, string file_name<256>,string file_description<256>) = 3;
        char my_delete(string user_name<256>, string file_name<256>) = 4;
        char my_connect(string user_name<256>, string clientip<20>,string client_port<8>) = 5;
        char disconnect(string user_name<256>) = 6;
    } = 1;
} = 99;
