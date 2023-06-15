import requests
from bs4 import BeautifulSoup
import re 
import json
import geopy.distance
import pandas as pd
import time

def get_attributes(url, verbose = False):

    response = requests.get(url)

    if response.status_code == 500: return "BROKEN LINK"

    soup = BeautifulSoup(response.content, "html.parser")

    for i in soup.find_all("script"):
        if "var tree" in i.get_text():
            text = i.get_text()

            x = re.split(r'{|}', text)
            x = '{'+' '.join(x[1:-1])+'}'

            x = x.replace('": "', '": "None","').replace('"recorder": "name"', '"recorder": "None", "name"').replace('[','{').replace(']','}').replace('"species": "name"', '"species": "None","species_name"').replace('"species": "None","name"', '"species": "None","species_name"').replace('"veteranStatus": "name":','"veteranStatusBLANK": "BLANK","veteranStatus":').replace('"veteranStatus": "None","name":', '"veteranStatus": "None","veteranStatus":').replace('"publicAccessibilityStatus": "None","name":', '"publicAccessibilityStatusBLANK": "None","publicAccessibilityStatus":')

            dictionary = json.loads(x)

            if verbose:
                for key in dictionary:
                    print(key,':    ', dictionary[key],'\n')
            
            # attributes_to_keep = ["gridReference", "latitude", "longitude", "species_name", "measuredGirth", "localName", "veteranStatus", "publicAccessibilityStatus"]
            attributes_to_keep = ["latitude", "longitude", "species_name"]

            attributes = {}
            for attribute in attributes_to_keep:
                attributes[attribute] = dictionary[attribute]

            # attributes['url'] = url

            return attributes

# my_location = (51.4613663, -2.5538767) # lat, long
start_time = time.time()
N_samples = 245000
print("Scraping...")
for tree_id in range(1, N_samples):
    try:
        attributes = get_attributes(f"https://ati.woodlandtrust.org.uk/tree-search/tree?treeid={tree_id}#/")
        print('\n')
        # print(tree_id, attributes["species_name"], attributes["longitude"], attributes["latitude"], f'distance from Bristol: {geopy.distance.geodesic(my_location, (attributes["latitude"],attributes["longitude"])).km:.2f} km')
        for attribute in attributes:
            print(attribute,':  ',attributes[attribute])
        try:
            df_i = pd.DataFrame(attributes, index=[tree_id])
            df = pd.concat([df, df_i])
        except:
            df = pd.DataFrame(attributes, index=[tree_id])
    except: 
        pass
        # print(f"Broken {tree_id}")

    if tree_id % 10 == 0 and tree_id > 0:
        current_time = time.time()
        elapsed_time = current_time-start_time
        print(f"{tree_id}, {df.shape}, elapsed_time: {elapsed_time}, time per sample: {elapsed_time/tree_id}, expected time remaining: {(N_samples-tree_id)*(elapsed_time/tree_id)}")

print(df)

df.to_csv('trees.csv',header=False, index=False)








