using ApiAndroid.Data;
using ApiAndroid.Entities;
using ApiAndroid.Entities.DTOs;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Query.Internal;

namespace ApiAndroid.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ProductController : ControllerBase
    {
        public readonly DatabaseContext _context;

        public ProductController(DatabaseContext context)
        {
            _context = context;
        }

        [HttpGet("getProducts")]
        public async Task<ActionResult<List<ProductDTO>>> GetProducts()
        {
            try
            {
                var query = _context.Products.Include(p => p.Documents).Include(p => p.Photos); ;
                return Ok(await query.ToListAsync());
            }
            catch
            {
                return BadRequest();
            }
        }

        [HttpDelete("deleteProduct/{id:int}")]
        public async Task<ActionResult> DeleteProduct(int id)
        {
            try
            {
                var product = _context.Products.FirstOrDefault(x => x.ProductId == id);
                if (product != null)
                {
                    _context.Products.Remove(product);
                    await _context.SaveChangesAsync();
                    return Ok();
                }
                return NotFound("No se encontro producto con Id " + id);
            }
            catch
            {
                return BadRequest();
            }
        }

        [HttpGet("getProductById/{id}")]
        public async Task<ActionResult<List<ProductDTO>>> GetProductById(int id)
        {
            try
            {
                var query = _context.Products.Include(p => p.Documents).Include(p => p.Photos).Where(p=> p.ProductId == id);
                return Ok(await query.ToListAsync());
            }
            catch
            {
                return BadRequest();
            }
        }

        [HttpPut("updateProduct/{id}")]
        public async Task<ActionResult<List<ProductDTO>>> UpdateProduct([FromRoute]int id, [FromBody] Product product)
        {
            try
            {
                var query = await _context.Products.Where( x => x.ProductId == id).FirstOrDefaultAsync();
                if (query == null)
                    return NotFound();

                var existingPhotos = _context.ProductPhotos.Where(p => p.ProductId == id).ToList();
                var existingDocuments = _context.Documents.Where(d => d.ProductId == id).ToList();

                _context.ProductPhotos.RemoveRange(existingPhotos);
                _context.Documents.RemoveRange(existingDocuments);

                query.ProductNumber = product.ProductNumber;
                query.ProductName = product.ProductName;
                query.Photos = product.Photos;
                query.Documents = product.Documents;

                await _context.SaveChangesAsync();
                return Ok();
            }
            catch
            {
                return BadRequest();
            }
        }

        [HttpPost("createProduct")]
        public async Task<ActionResult> CreateProduct(ProductDTO product)
        {
            try
            {
                var products = _context.Products;
                products.Add(new Product
                {
                    ProductName = product.ProductName,
                    ProductNumber = product.ProductNumber,
                    Photos = product.Photos.Select(p => new ProductPhoto
                    {
                        LargePhoto = p.LargePhoto
                    }).ToList(),

                    Documents = product.Documents.Select(d => new Document
                    {
                        Title = d.Title,
                        FileName = d.FileName,
                        FileExtension = d.FileExtension,
                        Revision = d.Revision,
                        DocumentFile = d.DocumentFile,

                    }).ToList()
                });
                await _context.SaveChangesAsync();
                return Ok();
            }
            catch
            {
                return BadRequest();
            }
        }
    }


}
