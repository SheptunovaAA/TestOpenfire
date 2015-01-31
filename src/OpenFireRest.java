import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by Alena_Fox on 31.01.2015.
 */
public class OpenFireRest{

        public static String serverName = "e178dec24a3e";
        public static  String serverAddress = "217.20.149.125";
        public static String serverPort = "4002";
        //используется для авторизации при удаленном управлении пользователями OpeFire
        private static String secretKey = "EAWgo8p3";

        private static final String ADD_USER_URL = "/plugins/userService/userservice?type=add&secret={secret}&username={userName}&password={userPass}&name={userName}&email={userName}";
        private static final String DEL_USER_URL = "/plugins/userService/userservice?type=delete&secret={secret}&username={userName}";


        public static  String createNewUser (String userName) throws Exception {
            String userPass = generateUserPass(userName);

            sendAddUserRequest(userName, userPass);
            return userName + "@" + serverName;
        }

        private static  void sendAddUserRequest(String userName, String userPass) throws Exception {
            RestTemplate template = new RestTemplate();
            String url = makeURL(ADD_USER_URL);
            try {
                //TODO: add unmarshaler from xml to our object
                String response = template.getForObject(url, String.class,  secretKey, userName,  userPass, userName, userName);
                checkResponse(response);

            }
            catch (RestClientException e){
                e.printStackTrace();
                throw new Exception("erorr while connectiong to OpenFire server");
            }
        }

        private static  void checkResponse(String response) throws Exception{
            if(response.contains("error")){
                throw new Exception("Can't add user:" + response + "\n");
            }
        }

        private static  String makeURL(String cmd) {
            return "http://" + serverAddress + ":" + serverPort + cmd;
        }

        private static String generateUserPass(String userName) {
            return userName;
        }

        public static void deleteUser(String userId) throws Exception {
            RestTemplate template = new RestTemplate();
            String url = makeURL(DEL_USER_URL);
            try {
                //TODO: add unmarshaler from xml to our object
                String response = template.getForObject(url, String.class, secretKey, userId);
                checkResponse(response);
            }
            catch (RestClientException e){
                e.printStackTrace();
                throw new Exception("Can't delete user\n");
            }
        }
    }

