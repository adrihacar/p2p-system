
struct user {
    string name<256>;
    string ip <20>;
    string port <8>;
};

typedef luser user<>;

struct ruser {
    int err;
    luser l;
};

struct content {
    string fileName<256>;
};

typedef lcontent content<>;

struct rcontent {
    int err;
    lcontent l;
};

program PROG{
    version PROGVER{
        void init_database() = 1;
        char my_register(string user_name<256>) = 2;
        char unregister(string user_name<256>) = 3;
        char publish(string user_name<256>, string file_name<256>,string file_description<256>) = 4;
        char my_delete(string user_name<256>, string file_name<256>) = 5;
        char my_connect(string user_name<256>, string clientip<20>,string client_port<8>) = 6;
        char disconnect(string user_name<256>) = 7;
        ruser list_users(string user_name<256>) = 8;
        rcontent list_content(string user_name<256>) = 9;
    } = 1;
} = 99;
