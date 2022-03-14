import os

x = input("Press Enter after ensuring all the services are up and running")
print("\n\nWhich set of test cases do you want to use")
print("1. Public Test Cases Both sequential and concurrent (Enter input 1)")
print("2. Sequential Test Cases (Enter input 2)")
print("3. Concurrent Test Cases (Enter input 3)")
print("Any other key to exit")

choice = input()
if(choice.isnumeric):
    choice = int(choice)
    if choice == 1 or choice==2 or choice == 3:
        if choice == 1:
            os.chdir("public-test-cases")
        elif choice == 2:
            os.chdir("self-test-cases")
        else:
            os.chdir("concurrent-test-cases")
        files = os.listdir()
        for f in files:
            if(f.endswith(".py")):
                print("\n\nTest Case : " + f)
                os.system("python3 " + f)
                x = input("Press any key to start the next test case")


    







