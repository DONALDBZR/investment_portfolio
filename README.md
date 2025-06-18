# Investment Portfolio

## Description
Investment Portfolio is a modern web application designed to help users track and manage their personal investment assets with ease.  Built with a powerful stack including React for the frontend and SpringBoot for the backend, it provides real-time portfolio valuation, asset allocation, and performance tracking.

### **Features:**
- Tracking investments done on the Peer-to-Peer Lending Platform [FinClub](https://www.finclub.mu).

## Usage
1. An Investor can login on his/her account.

## Commit History (Latest Major Updates)
- **[UPDATE 0.0.69]** Validating the existence and freshness of a specified file based on its creation timestamp.
- **[UPDATE 0.0.67]** Authenticating the user by either retrieving a cached session or performing a login request to the FinClub API.
- **[UPDATE 0.0.63]** Reading and deserializing the cached response data from the specified JSON file.
- **[UPDATE 0.0.61]** Serializing the provided response object into a pretty-printed JSON format and writing it to the specified file path.
- **[UPDATE 0.0.58]** Authenticating the user by either retrieving a cached session or performing a login request to the FinClub API.
- **[UPDATE 0.0.56]** Validating whether the file's data is still considered valid based on the given time constraints.
- **[UPDATE 0.0.54]** The model responsible for all processing related to file operations on the server.
- **[UPDATE 0.0.52]** Model responsible for processing requests related to the FinClub external API.
- **[UPDATE 0.0.49]** Handling user login by sending authentication credentials to the external FinClub API and caching the response locally.
- **[UPDATE 0.0.46]** Constructing a controller with all required dependencies and credentials for performing authentication requests against the external FinClub API.
- **[UPDATE 0.0.42]** Sending a login request to the FinClub API with the specified endpoint and payload, then caching the response as a JSON file in the given cache directory.
- **[UPDATE 0.0.41]** Saving a given response object to a specified path in a human-readable JSON format as well as creating any missing parent directories.
- **[UPDATE 0.0.36]** Constructing the model by injecting the REST Template which allows the model to communicate with external services, as well as injecting the Object Mapper which handles the JSON processing.
- **[UPDATE 0.0.11]** Writing the API response to a JSON file on the server.
- **[UPDATE 0.0.1]** Initial commit and setting up the project.

## Contributing
Contributions, feedback, and testing are welcome!  Please open a pull request or issue for any modifications.  The main branch is reserved for production; development should be done in separate branches.

## License
This project is licensed under the CeCILL Free Software License Agreement (Version 2.1).  For full details, visit: [CeCILL License](http://www.cecill.info/index.en.html).

## Contact
For inquiries or support, feel free to open an issue on GitHub.

