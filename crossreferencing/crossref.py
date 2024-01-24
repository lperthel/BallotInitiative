import pandas as pd
import numpy as np
import pdb

"""
0. Scanned file should have a combination of Full Name, (full) Street Address
1. check name
2. check combination of Street_Number, Street_Name, Street_Type
       A. check Street_Dir_Suffix if the street is NTH, ALPHABET LETTER, Penn. Ave, Mass. Ave, Independence Ave, or Capitol St
"""

CRITICAL_COLS = ['Last_Name', 'First_Name', 'Street_Number',
       'Street_Name', 'Street_Type', 'Street_Dir_Suffix', 'Unit_Type',
       'Apartment_Number', 'WARD']

def get_matches(scanned_filename: str, registry_filename: str) -> pd.DataFrame:
      FULL_NAME = 'full_name'
      voter_registry = pd.read_csv(registry_filename, usecols=CRITICAL_COLS[:6])
      ocr_df = pd.read_csv(scanned_filename) #, usecols=CRITICAL_COLS[:5])
      pdb.set_trace()

      def combine_columns(row):
            return f"{row[CRITICAL_COLS[1]]} {row[CRITICAL_COLS[0]]}"
      
      ocr_df[FULL_NAME] = ocr_df.apply(combine_columns, axis=1)
      ocr_df[FULL_NAME] = ocr_df[FULL_NAME].map(lambda s: s.upper()).astype(str)
      voter_registry[FULL_NAME] = voter_registry.apply(lambda row: f"{row.First_Name} {row.Last_Name}".upper(), axis = 1)
      voter_registry[FULL_NAME] = voter_registry[FULL_NAME].astype(str)
      
      ocr_df.set_index(FULL_NAME)
      voter_registry.set_index(FULL_NAME)
      
      df_fullname_match = ocr_df.merge(voter_registry, on=FULL_NAME, how='left')
#       non_matches = df_fullname_match[df_fullname_match['Last_Name_x'].isna()]
      return df_fullname_match

print(get_matches("../Aggregated Data/output.csv",
                  "./voter_registries/raw_feb_23_city_wide.csv"))

"""
df = pd.read_csv("raw_feb_23_city_wide.csv", usecols=['Last_Name', 'First_Name', 'Street_Number', 'Street_Name'])
deck_indices = df.index[df['Last_Name'] == 'Deck'].tolist()
df.iloc[deck_indices]
"""