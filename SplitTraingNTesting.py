import sys
import math
import os
import glob


def newfile(FileName):
        #Define proportion
        proportion = 0.5
        #Edify the name
        name = FileName+'.txt'
        #Open file
        open(name,'r')
        print('Creating new text file')
        #50% training data
        AFileName = 'train_'+name
        #5% testing data
        BFileName = 'test_'+name
        #Read content of the file
        content = open(name).readlines()
        
        number_of_lines = len(content)
        num=int(number_of_lines * proportion)

        #Split the file
        first_portion = "\n".join(content[:num])
        second_portion = "\n".join(content[num:])
        #Write into new file
        open(AFileName,"w").write(first_portion)
        open(BFileName,"w").write(second_portion)


newfile("2-1-01")
newfile("2-1-02")
newfile("2-1-03")
newfile("2-1-04")
newfile("2-1-05")

newfile("2-2-01")
newfile("2-2-02")
newfile("2-2-03")
newfile("2-2-04")
newfile("2-2-05")

newfile("spaceBar-01")
newfile("spaceBar-02")
newfile("spaceBar-03")
newfile("spaceBar-04")
newfile("spaceBar-05")

newfile("concourse-01")
newfile("concourse-02")
newfile("concourse-03")
newfile("concourse-04")
newfile("concourse-05")
newfile("concourse-06")
newfile("concourse-07")
newfile("concourse-08")
newfile("concourse-09")
newfile("concourse-10")
newfile("concourse-11")

newfile("canteen-01")
newfile("canteen-02")
newfile("canteen-03")
newfile("canteen-04")
newfile("canteen-05")
newfile("canteen-06")

newfile("LT-5-01")
newfile("LT-5-02")
newfile("LT-5-03")
newfile("LT-5-04")
newfile("LT-5-05")

newfile("LT-4-01")
newfile("LT-4-02")
newfile("LT-4-03")
newfile("LT-4-04")
newfile("LT-4-05")


            

            
