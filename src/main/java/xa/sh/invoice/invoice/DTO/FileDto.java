package xa.sh.invoice.invoice.DTO;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public class FileDto {
    private String company ;
    private String address;
    private String phone;
    private String email;
    private String Gstin;
    private MultipartFile logo;
    private String customerName;
    private String customerEmail;
    private String customerGstin;
    private int invoiceNumber;
    private List<ItemDto> items;


    //Signature 
    private MultipartFile keystore; // p12 file
private String keystorePassword;
private String alias;



    public MultipartFile getKeystore(){
        return keystore;
    }

    public void setKeystore(MultipartFile keystore){
        this.keystore = keystore;
    }
    /**
     * @return String return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @param company the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * @return String return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * @return String return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return String return the Gstin
     */
    public String getGstin() {
        return Gstin;
    }

    /**
     * @param Gstin the Gstin to set
     */
    public void setGstin(String Gstin) {
        this.Gstin = Gstin;
    }

    /**
     * @return String return the customerName
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @param customerName the customerName to set
     */
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    /**
     * @return String return the customerEmail
     */
    public String getCustomerEmail() {
        return customerEmail;
    }

    /**
     * @param customerEmail the customerEmail to set
     */
    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    /**
     * @return List<ItemDto> return the items
     */
    public List<ItemDto> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<ItemDto> items) {
        this.items = items;
    }


    /**
     * @return String return the customerGstin
     */
    public String getCustomerGstin() {
        return customerGstin;
    }

    /**
     * @param customerGstin the customerGstin to set
     */
    public void setCustomerGstin(String customerGstin) {
        this.customerGstin = customerGstin;
    }


    /**
     * @return MultipartFile return the logo
     */
    public MultipartFile getLogo() {
        return logo;
    }

    /**
     * @param logo the logo to set
     */
    public void setLogo(MultipartFile logo) {
        this.logo = logo;
    }


    /**
     * @return String return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }


    /**
     * @return String return the keystorePassword
     */
    public String getKeystorePassword() {
        return keystorePassword;
    }

    /**
     * @param keystorePassword the keystorePassword to set
     */
    public void setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
    }

    /**
     * @return String return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }


    /**
     * @return int return the invoiceNumber
     */
    public int getInvoiceNumber() {
        return invoiceNumber;
    }

    /**
     * @param invoiceNumber the invoiceNumber to set
     */
    public void setInvoiceNumber(int invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

}
