## Android LibRTSP

This is a very simple and straight-forward RTSP "library" for Android. It can used to create both TCP and UDP client or server implementations. It can be used to test any server or client that uses TCP or UDP protocol to communicate. 

### Disclaimer

This repository contains sample code intended to demonstrate the capabilities of the Exoplayer using a custom RTSP library. The current version of the code supports RTP over UDP and TCP as the transport protocol and only decodes H264 encoded video. It is not intended to be used **as-is** in applications as a library dependency, and will not be maintained as such. Bug fix contributions are welcome, but issues and feature requests will not be addressed.

### Design:

This project provides a basic implementation for RTSP and RTP protocol communication. Also, encapsulates the Exoplayer as decoder in the `RtspPlayer` class.

### Contributing
If you would like to contribute code, you can do so through GitHub by forking the repository and sending a pull request.
When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

### Pre-requisites

- Android SDK 25
- Android Build Tools v25.0.2
- Android Support Repository


## Credits

This project was based on the huge knowledge learn from the **libstreaming** project.

- [libstreaming][1]

## License

The code supplied here is covered under the MIT Open Source License..

[1]: https://github.com/fyhertz/libstreaming



