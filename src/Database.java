import java.io.*;
import java.sql.*;
import java.time.LocalDate;

public class Database {

    private Connection conn = null;
    private Connection updateConnection = null;
    String url ;
    Database(){
        String location = null;
        try{
            File configFile = new File("./res/CONFIG.config");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                String[] arr = line.split("=");
                if(arr[0].equals("path_to_res"))
                    location = arr[1];
            }
            if(location == null){
                SetupManager setupManager = new SetupManager();
                location = setupManager.getFs();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        this.url = String.format("jdbc:sqlite:%s/res/Upepeo.db", location);
        System.out.println(this.url);
        try {
            //Class.forName("org.sqlite.JDBC");
            // db parameters
            // create a connection to the database
            conn = DriverManager.getConnection(this.url);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        finally {
            closeConnection();
        }

        try {
            this.updateConnection = DriverManager.getConnection(this.url);
            System.out.println("Update connection has been established");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }finally {
            closeUpdateConnection();
        }
    }

    private void openUpdateConnection(){
        try {
            this.updateConnection = DriverManager.getConnection(this.url);
            System.out.println("Update connection has been established");
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private void closeUpdateConnection(){
        try {
            this.updateConnection.close();
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    private void openConnection(){
        try {
            // db parameters
            // create a connection to the database
            conn = DriverManager.getConnection(this.url);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    public void closeConnection(){
        try {
            if (this.conn != null) {
                this.conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }
    public boolean isUnique(String data, String table, String field){
        openConnection();
        boolean unique = false;
        String query = "SELECT * FROM " + table + " WHERE " + field + "='" + data + "'";
        try {
            Statement statement = this.conn.createStatement();

            ResultSet resultSet = statement.executeQuery(query);
            if(!resultSet.next()) {
                unique = true;
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }finally {
            closeConnection();
        }
        return unique;
    }

    public int addMember(String[] data){
        //first_name, surname, phone_number, res_address, date_added, national_id
        openUpdateConnection();
        int temp = 0;
        Date now = Date.valueOf(LocalDate.now());
        String sql = "INSERT INTO Members(first_name, surname, phone_number, Residential_address, date_added, national_id, membership_number) VALUES ('" +
                data[0] + "', '" +
                data[1] + "', '" +
                data[2] + "', '" +
                data[3] + "', '" +
                now.toString() + "', '" +
                data[4] + "', '" +
                data[5] + "')";
        try {
            Statement statement  = this.updateConnection.createStatement();
            temp = statement.executeUpdate(sql);
            System.out.println("User inserted");
            // loop through the result set
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            temp = -1;
        }
        finally {
            closeUpdateConnection();
        }
        return temp;
    }
    public int addVideo(String videoName, int videCategory){
        Date now = Date.valueOf(LocalDate.now());
        this.openUpdateConnection();
        int result = 0;
        String query = "INSERT INTO videos (video_name, video_category, video_status, date_added) VALUES ( '"
                + videoName + "', " + videCategory + ", 1, '" + now.toString() + "')";
        try{
            Statement statement = this.updateConnection.createStatement();
            result = statement.executeUpdate(query);
            System.out.println("Video Added");
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            result = -1;
        }finally {
            closeUpdateConnection();
        }
        return result;
    }

    public int getUserId(String mem_number){
        int id = -2;
        openConnection();
        String query = "SELECT * FROM members WHERE membership_number='" + mem_number + "'";
        try{
            Statement statement = this.conn.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            while(resultSet.next()){
                System.out.println(resultSet.getInt("user_id"));
                id = resultSet.getInt("user_id");
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            id = -1;
        }finally {
            closeConnection();
        }
        return id;
    }
    public void selectAll(){
        String sql = "SELECT * FROM members where membership_number='0000'";
        openConnection();
        try {
            Statement stmt  = this.conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);

            // loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("first_name"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }finally {
            closeConnection();
        }
    }

    public String[] getVideoInformation(String query){
        String[] info = new String[6];
        String sql = "SELECT * FROM videos WHERE video_id=" + query + " OR video_name='" + query + "'";
        openConnection();
        try{
            Statement st = this.conn.createStatement();
            ResultSet videoInfo = st.executeQuery(sql);
            while (videoInfo.next()){
                info[0] = videoInfo.getString("video_id");
                info[1] = String.valueOf(videoInfo.getInt("video_category"));
                info[2] = String.valueOf(videoInfo.getInt("video_status"));
                info[3] = videoInfo.getString("video_name");
                info[4] = String.valueOf(videoInfo.getInt("borrower"));
                info[5] = videoInfo.getString("date_last_borrowed");
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            info[0] = "-1";
        }finally {
            closeConnection();
        }
        return info;
    }

    public int borrowVideo(int video_id, int user_id){
        int res = 0;
        openUpdateConnection();
        Date now = Date.valueOf(LocalDate.now());
        String query = "UPDATE videos " +
                "SET borrower=" + user_id + ", date_last_borrowed='" + now.toString() + "', video_status=4 WHERE video_id=" + video_id;
        try{
            Statement statement = this.updateConnection.createStatement();
            res = statement.executeUpdate(query);
            System.out.println("Video Borrowed");
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            res = -1;
        }finally {
            closeUpdateConnection();
        }
        return res;
    }

    int returnVideo(int video_id, String videoReport, int videoStatus){
        int res = 0;
        openUpdateConnection();
        String query = "UPDATE videos " +
                "SET borrower=null, video_status="+ videoStatus + ", latest_report='" + videoReport + "' WHERE video_id=" + video_id;
        try{
            Statement statement = this.updateConnection.createStatement();
            res = statement.executeUpdate(query);
            System.out.println("Video Borrowed");
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            res = -1;
        }finally {
            closeUpdateConnection();
        }
        return res;
    }

    public int checkPendingVideos(int user_id){
        openConnection();
        int video_count = 0;
        String queryString = "SELECT * FROM videos WHERE video_status=4 AND borrower=" + user_id;
        try {
            Statement st = this.conn.createStatement();
            ResultSet rs = st.executeQuery(queryString);
            while(rs.next()){
                video_count++;
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
        }finally {
            closeConnection();
        }
        return video_count;
    }

    public int getTotalAmount(){
        int total = 0;
        openConnection();
        String queryString = "SELECT * FROM finance";
        try {
            Statement st = this.conn.createStatement();
            ResultSet rs = st.executeQuery(queryString);
            while(rs.next()){
                total += rs.getInt("amount");
            }
        }catch (SQLException e){
            total = -1;
            e.printStackTrace();
            System.out.println(e.getMessage());
        }finally {
            closeConnection();
        }
        return total;
    }

    int carryTransaction(int amount, int type){
        int res = 0;
        openUpdateConnection();
        Date now = Date.valueOf(LocalDate.now());
        String query = "INSERT INTO finance (amount, date_added, transaction_type) VALUES (" + amount +", '" + now.toString() + "', " + type + ")";
        try{
            Statement statement = this.updateConnection.createStatement();
            res = statement.executeUpdate(query);
            System.out.println("New Transaction made");
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            res = -1;
        }finally {
            closeUpdateConnection();
        }
        return res;
    }

    public int getBill(String mem_num){
        openConnection();
        int bill_amount = 0;
        String queryString = "SELECT * FROM members WHERE membership_number='" + mem_num + "'";
        try {
            Statement st = this.conn.createStatement();
            ResultSet rs = st.executeQuery(queryString);
            while(rs.next()){
                bill_amount = rs.getInt("bill_balance");
            }
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println(e.getMessage());
            bill_amount = -1;
        }finally {
            closeConnection();
        }
        return bill_amount;
    }

    public int payBill(String mem_num){
        int res = 0;

        openUpdateConnection();
        String query = "UPDATE members SET bill_balance=0 WHERE membership_number='" + mem_num + "'";
        try{
            Statement statement = this.updateConnection.createStatement();
            res = statement.executeUpdate(query);
            System.out.println("Bill repaid");
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            res = -1;
        }finally {
            closeUpdateConnection();
        }
        return res;
    }
    public void addToBill(int amount, String mem_num){
        int res = 0;
        int pending_bill = getBill(mem_num);
        openUpdateConnection();
        String query = "UPDATE members SET bill_balance=" + (pending_bill + amount) + " WHERE membership_number='" + mem_num + "'";
        try{
            Statement statement = this.updateConnection.createStatement();
            res = statement.executeUpdate(query);
            System.out.println("Bill added");
        }catch (SQLException e){
            System.out.println(e.getMessage());
            e.printStackTrace();
            res = -1;
        }finally {
            closeUpdateConnection();
        }
    }
}
