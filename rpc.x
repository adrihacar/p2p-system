
program REGISTER{
    version REGVER{
        char my_register(string user_name<256>) = 1;
    } = 1;
} = 99;

program UNREGISTER{
    version UNREGVER{
        char unregister(string user_name<256>) = 1;
    } = 1;
} = 98;

program PUBLISH{
    version PUBVER{
        char publish(string user_name<256>, string file_name<256>,string file_description<256>) = 1;
    } = 1;
} = 97;

program DELETE{
    version DELVER{
        char my_delete(string user_name<256>, string file_name<256>) = 1;
    } = 1;
} = 96;

program CONNECT{
    version CONVER{
        char my_connect(string user_name<256>, string clientip<20>,string client_port<8>) = 1;
    } = 1;
} = 95;

program DISCONNECT{
    version DISCONVER{
        char disconnect(string user_name<256>) = 1;
    } = 1;
} = 95;


char disconnect (char * user_name);


