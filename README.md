# Investment Portfolio

## Description
Investment Portfolio is a modern web application designed to help users track and manage their personal investment assets with ease.  Built with a powerful stack including React for the frontend and SpringBoot for the backend, it provides real-time portfolio valuation, asset allocation, and performance tracking.

### **Features:**
- Tracking investments done on the Peer-to-Peer Lending Platform [FinClub](https://www.finclub.mu).

## Usage
1. An Investor can login on his/her account.

## Commit History (Latest Major Updates)
- **[UPDATE 0.1.54]** Transformed raw escrow account data from the FinClub API into a structured map summarizing credit and debit.
- **[UPDATE 0.1.53]** Added and enhanced a method for safely parsing an `Object` into a `Float`.
- **[UPDATE 0.1.51]** Formatted API data into a map with clearly separated float values for credit and debit.
- **[UPDATE 0.1.50]** Repeated implementations and refinements of retrieving escrow account overview for the current user.
- **[UPDATE 0.1.45]** Introduced utility classes for error logging.
- **[UPDATE 0.1.43]** Initial retrieval of escrow account overview from the FinClub API.
- **[UPDATE 0.1.41]** Validated and extracted authentication tokens from provided data.
- **[UPDATE 0.1.40]** Defined a custom exception for invalid/missing tokens.
- **[UPDATE 0.1.38]** Implementation of user login by forwarding credentials to the FinClub API.
- **[UPDATE 0.1.37]** Standardized error handling during request processing.
- **[UPDATE 0.1.36]** Added private constructors to utility classes to prevent instantiation.
- **[UPDATE 0.1.34]** Reading, deserializing, and verifying the authentication cache file.
- **[UPDATE 0.1.30]** Controller logic for handling root and investor-specific routes.
- **[UPDATE 0.1.29]** Retrieve user authentication from either cache or API.
- **[UPDATE 0.1.16]** Added utility methods for verifying trusted IPs, localhost addresses, and private network checks.
- **[UPDATE 0.1.4]** Introduced custom exception for invalid access attempts.
- **[UPDATE 0.1.3]** Validated requests originate from the same machine.
- **[UPDATE 0.1.1]** Initial implementation of Authentication Controller.

## Contributing
Contributions, feedback, and testing are welcome!  Please open a pull request or issue for any modifications.  The main branch is reserved for production; development should be done in separate branches.

## License
This project is licensed under the CeCILL Free Software License Agreement (Version 2.1).  For full details, visit: [CeCILL License](http://www.cecill.info/index.en.html).

## Contact
For inquiries or support, feel free to open an issue on GitHub.

