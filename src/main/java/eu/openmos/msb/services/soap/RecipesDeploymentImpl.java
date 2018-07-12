package eu.openmos.msb.services.soap;

import eu.openmos.model.ExecutionTable;
import eu.openmos.model.Recipe;
import eu.openmos.model.Recipe_DA;
import eu.openmos.msb.database.interaction.DatabaseInteraction;
import eu.openmos.msb.datastructures.DACManager;
import eu.openmos.msb.datastructures.DeviceAdapter;
import eu.openmos.msb.opcua.milo.client.MSBClientSubscription;
import eu.openmos.msb.services.rest.SystemStageController;
import eu.openmos.msb.utilities.Functions;
import java.util.List;
import javax.jws.WebService;
import org.apache.log4j.Logger;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

/**
 *
 * @author Valerio Gentile <valerio.gentile@we-plus.eu>
 */
@WebService(endpointInterface = "eu.openmos.msb.services.soap.RecipesDeployment", serviceName = "RecipesDeployment")
public class RecipesDeploymentImpl implements RecipesDeployment
{

  public static int SUGGESTION = 1;
  public static int ACTUAL = 2;
  public static int ACTIVATION = 3;
  private static final Logger logger = Logger.getLogger(RecipesDeploymentImpl.class.getName());

  @Override
  public boolean updateRecipes(String da_id, int mode, List<Recipe> recipes)
  {

    Boolean ret = false;
    logger.debug("updateRecipes MSB method");
    logger.debug("device ID = [" + da_id + "]");

    DeviceAdapter da = DACManager.getInstance().getDeviceAdapterbyAML_ID(da_id);

    if (da != null)
    {
      logger.info("[RecipesDeploymentImpl] Received an update Recipe call from Cloud to DA: " + da.getSubSystem().getName());

      logger.debug("mode = [" + mode + "]");
      if (mode == RecipesDeploymentImpl.SUGGESTION)
      {
        logger.debug("suggested recipes mode");
        //updated:*******
        for (Recipe newRecipe : recipes)
        {
          boolean found = false;
          for (Recipe recipe : da.getSubSystem().getRecipes())
          {
            if (recipe.getUniqueId().equals(newRecipe.getUniqueId()))
            {
              found = true;
              recipe.setDescription(newRecipe.getDescription());
              recipe.setEquipmentIds(newRecipe.getEquipmentIds());
              recipe.setExecutedBySkillControlPort(newRecipe.getExecutedBySkillControlPort());
              recipe.setInvokeMethodID(newRecipe.getInvokeMethodID());
              recipe.setInvokeObjectID(newRecipe.getInvokeObjectID());
              recipe.setKpiSettings(newRecipe.getKpiSettings());
              recipe.setLastOptimizationTime(newRecipe.getLastOptimizationTime());
              recipe.setMsbProtocolEndpoint(newRecipe.getMsbProtocolEndpoint());
              recipe.setName(newRecipe.getName());
              recipe.setOptimized(newRecipe.isOptimized());
              recipe.setParameterSettings(newRecipe.getParameterSettings());
              recipe.setRegistered(newRecipe.getRegistered());
              recipe.setSkill(newRecipe.getSkill());
              recipe.setSkillRequirements(newRecipe.getSkillRequirements());
              recipe.setState(newRecipe.getState());
              recipe.setStatePath(newRecipe.getStatePath());
              recipe.setUniqueAgentName(newRecipe.getUniqueAgentName());
              recipe.setUniqueId(newRecipe.getUniqueId());
              recipe.setValid(newRecipe.isValid());
            }
          }
          if (!found)
          {
            da.getSubSystem().getRecipes().add(newRecipe);
          }
        }

        SystemStageController ssc = null;
        if (ssc != null && !ssc.getSystemStage().equals("PRODUCTION"))
        {
          int count = 0;
          for (int i = 0; i < recipes.size(); i++)
          {
            Recipe_DA recipe_DA = Recipe_DA.createRecipe_DA(recipes.get(i));
            MSBClientSubscription client = (MSBClientSubscription) da.getClient();
            String RecipeSerialized = Functions.ClassToString(recipe_DA);
            NodeId objectID = Functions.convertStringToNodeId(recipe_DA.getChangeRecipeObjectID());
            NodeId methodID = Functions.convertStringToNodeId(recipe_DA.getChangeRecipeMethodID());
            boolean updateRecipe = client.updateRecipe(client.getClientObject(), objectID, methodID, RecipeSerialized);
            if (updateRecipe)
            {
              count++;
            }
          }
          if (count == recipes.size())
          {
            return true;
          } else
          {
            return false;
          }
        }
        return true;
      } else
      {
        logger.error("DA for recipe update not found in the database!");
        return false;
      }

    } else if (mode == RecipesDeploymentImpl.ACTUAL)
    {
      logger.debug("actual recipes mode");

    } else if (RecipesDeploymentImpl.ACTIVATION == mode)
    {
      logger.debug("activation of already sent recipes mode");
      //msb should activate the selected Recipe (OPC namespace write?  method? )
    } else
    {
      logger.debug("unknown mode");
    }

    logger.debug("recipes list = [" + recipes + "]");

    return true;
  }

  @Override
  public boolean updateExecutionTable(ExecutionTable executionTable)
  {
    logger.debug("updateExecutionTable MSB method");
    logger.debug("executionTable: " + executionTable);

    String execTableUniqueId = executionTable.getUniqueId();
    List<String> deviceAdaptersID = DACManager.getInstance().getDeviceAdapters_AML_IDs();
    Boolean ret = false;

    for (String da_id : deviceAdaptersID)
    {
      DeviceAdapter deviceAdapter = DACManager.getInstance().getDeviceAdapterbyAML_ID(da_id);
      if (deviceAdapter != null)
      {
      if (deviceAdapter.getSubSystem().getName().toUpperCase().contains("MSB"))
      {
        continue;
      }
        if (deviceAdapter.getSubSystem().getExecutionTable().getUniqueId().equals(execTableUniqueId))
        {
          deviceAdapter.getSubSystem().setExecutionTable(executionTable);
          //MSBClientSubscription client = (MSBClientSubscription) deviceAdapter.getClient();

          /*try
          {
            File file = new File("updateExecTables.xml");
            javax.xml.bind.JAXBContext jc = JAXBContext.newInstance(ExecutionTable.class);
            Marshaller jaxbMArshaller = jc.createMarshaller();
            jaxbMArshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMArshaller.marshal(executionTable, System.out); //print in the console
            jaxbMArshaller.marshal(executionTable, file); //print in the file
            StringWriter sw = new StringWriter();
            jaxbMArshaller.marshal(executionTable, sw); //print to String
            //String excTablesString = XMLtoString("updateExecTables.xml"); //TODO: use outputStream instead of file!
            String excTablesString = sw.toString();
            
          } catch (JAXBException ex)
          {
            java.util.logging.Logger.getLogger(RecipesDeploymentImpl.class.getName()).log(Level.SEVERE, null, ex);
          }*/
          //client.InvokeExecTableUpdate(client, NodeId.NULL_GUID, NodeId.NULL_GUID, excTablesString); //TO be done by DA
          ret = true;
          logger.info("Sending new execution table to DA: " + deviceAdapter.getSubSystem().getName());
        } else
        {

        }
      }
    }

    return ret;
  }

}
